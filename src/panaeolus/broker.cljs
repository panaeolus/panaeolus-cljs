(ns panaeolus.broker
  (:require
   [cljs.core.async
    :refer [<! >! chan timeout] :as async]
   [panaeolus.engine
    :refer [poll-channel csound Csound BEAT
            pattern-registry bpm!
            TICKS-PER-SECOND]]
   [goog.string :as gstring])
  (:require-macros [cljs.core.async.macros
                    :refer [go go-loop]]))


(defn calc-mod-div [meter durations]
  (let [meter (if meter
                (if (< 0 meter) meter 0) 0)
        bar-length (* @TICKS-PER-SECOND meter)
        summed-durs (* @TICKS-PER-SECOND (apply + durations))]
    (if (< 0 meter)
      (* bar-length
         (inc (quot (dec summed-durs) bar-length)))
      summed-durs)))

(defn dur->event-queue [durations event-queue]
  (into event-queue
        (if (number? durations)
          (list durations)
          (loop [dur (remove #(or (neg? %)
                                  (zero? %)) durations)
                 silence 0
                 last-dur 0
                 at []]
            (if (empty? dur)
              at
              (let [fdur (first dur)]
                (recur (rest dur)
                       (if (neg? fdur)
                         (+ silence (* @TICKS-PER-SECOND (Math/abs fdur)))
                         0)
                       (if (neg? fdur)
                         last-dur
                         (* @TICKS-PER-SECOND fdur))
                       (if (neg? fdur)
                         at
                         (conj at ((fn [v]
                                     (if (float? v)
                                       (Math/round v) v))
                                   (+ last-dur
                                      silence
                                      (if (empty? at)
                                        0 (last at)))))))))))))

(quot 4 4)
(* 4 TICKS-PER-SECOND)

(dur->event-queue [0.1 0.1] #queue [])

(.EvalCode csound Csound
           "instr 2\nasig poscil 0.1, (100 + rnd(480))\nouts asig,asig\nendin")

(pattern-loop-queue
 {:dur [1 1 1 1]
  :pattern-name :e
  :meter 0 :kill true
  })

(bpm! 20)
(let [a #queue [1 2 3]]
  (print (pop (pop a))))

(defn pattern-loop-queue [env]
  (if-let [user-input-channel (get @pattern-registry (:pattern-name env))]
    (go (>! user-input-channel env))
    (let [{:keys [dur pattern-name meter]} env
          user-input-channel (chan (async/sliding-buffer 1))
          engine-poll-channel (chan)
          initial-queue (dur->event-queue dur #queue [])
          initial-mod-div (calc-mod-div meter dur)]
      (swap! pattern-registry assoc pattern-name user-input-channel)
      (go-loop [index 0
                a-index 0
                mod-div initial-mod-div
                mod-div-buffer initial-mod-div
                meter meter
                queue initial-queue
                queue-buffer initial-queue
                new-user-data nil]
        (let [{:keys [pause kill dur]} new-user-data
              [queue-buffer mod-div-buffer]
              (if dur
                [(dur->event-queue dur #queue [])
                 (calc-mod-div meter dur)]
                [queue-buffer mod-div-buffer])]
          (if kill
            (swap! pattern-registry dissoc pattern-name user-input-channel)
            (if-let [next-event (peek queue)]
              (do (go (>! poll-channel [next-event mod-div engine-poll-channel]))
                  (when (<! engine-poll-channel)
                    (println queue)
                    (.InputMessage csound Csound "i 2 0 0.1")
                    (recur (inc index)
                           (inc a-index)
                           mod-div
                           mod-div-buffer
                           meter
                           (pop queue)
                           queue-buffer
                           (async/poll! user-input-channel))))
              (recur (inc index)
                     (inc a-index)
                     mod-div-buffer
                     mod-div-buffer
                     meter
                     queue-buffer
                     queue-buffer
                     (async/poll! user-input-channel)))))))))


(go (let [event-c (chan)]
      (>! poll-channel [0 300 event-c])
      (when (<! event-c)
        (.InputMessage csound Csound "i 2 0 1"))))

(.CompileOrc csound Csound
             "instr 2\nasig poscil 0.1, 280\nouts asig,asig\nendin")

