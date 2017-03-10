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

(defn- calc-mod-div [meter durations]
  (let [meter (if meter
                (if (< 0 meter) meter 0) 0)
        bar-length meter
        summed-durs (apply + (map Math/abs durations))]
    (if (< 0 meter)
      (* bar-length
         (inc (quot (dec summed-durs) bar-length)))
      summed-durs)))

(defn- create-event-queue [durations input-messages] 
  (let [input-messages (if (string? input-messages)
                         [input-messages]
                         input-messages)]
    (into #queue []
          (if (number? durations)
            (list durations)
            (loop [dur (remove #(zero? %) durations)
                   msg input-messages
                   silence 0
                   last-dur 0
                   at []]
              (if (empty? dur)
                at
                (let [fdur (first dur)]
                  (recur (rest dur)
                         (if (neg? fdur)
                           msg
                           (if (empty? msg)
                             (rest input-messages)
                             (rest msg)))
                         (if (neg? fdur)
                           (+ silence (Math/abs fdur))
                           0)
                         (if (neg? fdur)
                           last-dur
                           fdur)
                         (if (neg? fdur)
                           at
                           (conj at [(+ last-dur
                                        silence
                                        (if (empty? at)
                                          0 (first (last at))))
                                     (or (first msg)
                                         (first input-messages))]))))))))))

(defn- calculate-timestamp [current-time mod-div beat]
  (let [current-beat (max (mod current-time mod-div) 0)
        delta (- beat current-beat)]
    (if (neg? delta)
      (+ beat current-time (- mod-div current-beat))
      (+ current-time delta))))


(defn pattern-loop-queue [env]
  ;; (prn env)
  (if-let [user-input-channel (get @pattern-registry (:pattern-name env))] 
    (go (>! user-input-channel env))
    (let [{:keys [dur pattern-name meter input-messages]} env
          user-input-channel (chan 0)
          engine-poll-channel (chan)
          initial-queue (create-event-queue dur input-messages)
          initial-mod-div (calc-mod-div meter dur)]
      (swap! pattern-registry assoc pattern-name user-input-channel)
      (go-loop [index 0
                a-index 0
                mod-div initial-mod-div
                mod-div-buffer initial-mod-div 
                meter meter
                queue initial-queue
                queue-buffer initial-queue
                new-user-data nil
                last-tick (.-beat Abletonlink)
                stop? false]
        ;; (println mod-div queue dur)
        (let [{:keys [pause kill stop? dur input-messages meter]
               :or {input-messages input-messages meter meter stop? stop?}} new-user-data
              [queue-buffer mod-div-buffer] (if dur
                                              [(create-event-queue dur input-messages)
                                               (calc-mod-div meter dur)]
                                              [queue-buffer mod-div-buffer])
              new-user-data nil] 
          (if kill
            (swap! pattern-registry dissoc pattern-name user-input-channel)
            (if-let [next-event (peek queue)] 
              (do  ;; (prn next-event)
                (go (>! poll-channel [(calculate-timestamp
                                       last-tick
                                       mod-div (first next-event))
                                      engine-poll-channel]))
                ;; (println "Reynir að skjóta")
                (when (<! engine-poll-channel)
                  ;; (println "KOM TIL SKILA")
                  (.ReadScore csound Csound (second next-event))
                  ;; (go (.InputMessage csound Csound (second next-event)))
                  ;; (println "BUINN AÐ SKILA!")
                  ;; (println "Skýtur")
                  (recur (inc index)
                         (inc a-index)
                         mod-div
                         mod-div-buffer
                         meter
                         (pop queue)
                         queue-buffer
                         (async/poll! user-input-channel)
                         (.-beat Abletonlink)
                         stop?)))
              (recur 0
                     (inc a-index)
                     mod-div-buffer
                     mod-div-buffer
                     meter
                     queue-buffer
                     queue-buffer
                     (if stop?
                       (<! user-input-channel)
                       (async/poll! user-input-channel))
                     (.-beat Abletonlink)
                     false))))))))


(comment 
  (pattern-loop-queue
   {:dur [1 1 1 -0.25 0.25 0.5]
    :pattern-name :a
    :input-messages "i 3 0 0.1 -3"
    :meter 4
    :kill true
    })

  (bpm! 361)
  (pattern-loop-queue
   {:dur [1 1 1 0.5 0.5]
    :pattern-name :c
    :input-messages "i 2 0 0.01 2"
    :meter 0
    :kill true
    })

  (dur->event-queue [1 1 1 1] #queue [])

  (go (let [event-c (chan)]
        (>! poll-channel [0 300 event-c])
        (when (<! event-c)
          (.InputMessage csound Csound "i 2 0 1"))))
  
  )
