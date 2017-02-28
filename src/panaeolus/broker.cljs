(ns panaeolus.broker
  (:require
   [cljs.core.async
    :refer [<! >! chan timeout] :as async]
   [panaeolus.engine
    :refer [poll-channel csound Csound
            pattern-registry bpm!
            Abletonlink ;;TICKS-PER-SECOND
            ]]
   [goog.string :as gstring])
  (:require-macros [cljs.core.async.macros
                    :refer [go go-loop]]))

(defn calc-mod-div [meter durations]
  (let [meter (if meter
                (if (< 0 meter) meter 0) 0)
        bar-length meter
        summed-durs (apply + durations)]
    (if (< 0 meter)
      (* bar-length
         (inc (quot (dec summed-durs) bar-length)))
      summed-durs)))

(defn dur->event-queue [durations event-queue]
  (into event-queue
        (if (number? durations)
          (list durations)
          (loop [dur (remove #(zero? %) durations)
                 silence 0
                 last-dur 0
                 at []]
            (if (empty? dur)
              at
              (let [fdur (first dur)]
                (recur (rest dur)
                       (if (neg? fdur)
                         (+ silence (Math/abs fdur))
                         0)
                       (if (neg? fdur)
                         last-dur
                         fdur)
                       (if (neg? fdur)
                         at
                         (conj at ((fn [v]
                                     (if (float? v)
                                       (Math/round v) v))
                                   (+ last-dur
                                      silence
                                      (if (empty? at)
                                        0 (last at)))))))))))))

(defn calculate-timestamp [current-time mod-div beat]
  (let [current-beat (mod current-time mod-div)
        delta (- beat current-beat)]
    (if (neg? delta)
      (+ beat current-time (- mod-div current-beat))
      (+ current-time delta))))

(defn pattern-loop-queue [env]
  (if-let [user-input-channel (get @pattern-registry (:pattern-name env))]
    (go (>! user-input-channel env))
    (let [{:keys [dur pattern-name meter]} env
          user-input-channel (chan 1)
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
                input-message-buffer (:input-message-buffer env)
                new-user-data nil]
        ;; (println "reload1")        
        (let [{:keys [pause kill dur input-message-buffer]
               :or {input-message-buffer input-message-buffer}} new-user-data
              [queue-buffer mod-div-buffer] (if dur
                                              [(dur->event-queue dur queue)
                                               (calc-mod-div meter dur)]
                                              [queue-buffer mod-div-buffer])
              new-user-data nil]
          ;; (println "reloead2")
          (if kill
            (swap! pattern-registry dissoc pattern-name user-input-channel)
            (if-let [next-event (peek queue)] 
              (do (println "reload3")
                  (go (>! poll-channel [(calculate-timestamp
                                         (.-beat Abletonlink)
                                         mod-div next-event)
                                        engine-poll-channel]))
                  (println "Reynir að skjóta")
                  (when (<! engine-poll-channel)
                    (println "KOM TIL SKILA")
                    (go (.InputMessage csound Csound input-message-buffer))
                    (println "BUINN AÐ SKILA!")
                    ;; (println "Skýtur")
                    (recur (inc index)
                           (inc a-index)
                           mod-div
                           mod-div-buffer
                           meter
                           (pop queue)
                           queue-buffer
                           input-message-buffer
                           (async/poll! user-input-channel))))
              (recur (inc index)
                     (inc a-index)
                     mod-div-buffer
                     mod-div-buffer
                     meter
                     queue-buffer
                     queue-buffer
                     input-message-buffer
                     (async/poll! user-input-channel)))))))))




(.CompileOrc csound Csound
             "instr 2\nasig poscil 0.1, 280\nouts asig,asig\nendin")

(.CompileOrc csound Csound
             "instr 3\nasig poscil 0.1, 480\nouts asig,asig\nendin")

(.CompileOrc csound Csound
             "instr 4\nasig poscil 0.1, 480\nouts asig,asig\nendin")

;; (.EvalCode csound Csound
;;            "instr 2\nasig poscil 0.1, (100 + rnd(480))\nouts asig,asig\nendin")


(comment 
  (pattern-loop-queue
   {:dur [-0.5 1 1 1 0.5]
    :pattern-name :a
    :input-message-buffer "i 2 0 0.1"
    :meter 4 :kill true
    })

  (bpm! 220)
  (pattern-loop-queue
   {:dur [1 1 0.5 0.5 0.4]
    :pattern-name :c
    :input-message-buffer "i 3 0 0.1"
    :meter 4
    :kill true
    })

  (dur->event-queue [1 1 1 1] #queue [])

  (go (let [event-c (chan)]
        (>! poll-channel [0 300 event-c])
        (when (<! event-c)
          (.InputMessage csound Csound "i 2 0 1"))))
  
  )
