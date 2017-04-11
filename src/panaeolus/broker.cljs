(ns panaeolus.broker
  (:require
   [cljs.core.async :refer [<! >! put! chan timeout] :as async]
   [panaeolus.engine :refer [poll-channel csound Csound
                             pattern-registry bpm!]]
   [goog.string :as gstring]
   [panaeolus.orchestra-parser :refer [ast-input-messages-builder]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


(defn- calc-mod-div [meter durations]
  (let [meter (if meter
                (if (< 0 meter) meter 0) 0)
        bar-length meter
        summed-durs (apply + (map Math/abs durations))]
    (* 20000
       (if (< 0 meter)
         (* bar-length
            (inc (quot (dec summed-durs) bar-length)))
         summed-durs))))

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
  (let [beat (* beat 20000)
        current-beat (max (mod current-time mod-div) 0)
        delta (- beat current-beat)]
    (if (neg? delta)
      (+ beat current-time (- mod-div current-beat))
      (+ current-time delta))))


(defn pattern-loop-queue [env]
  (if-let [user-input-channel (get @pattern-registry (:pattern-name env))] 
    (do ;;(prn env)
      (put! user-input-channel env)
      nil)
    (let [{:keys [dur pattern-name meter len input-messages]} env
          ;; _ (prn "INITIASL LEN" len)
          user-input-channel (chan 0)
          engine-poll-channel (chan)
          initial-queue (create-event-queue dur input-messages)
          initial-mod-div (calc-mod-div meter dur)
          initial-fx (:fx env)
          _ ((:recompile-fn env))]
      (swap! pattern-registry assoc pattern-name user-input-channel)
      (go-loop [index 0
                a-index 0
                mod-div initial-mod-div
                mod-div-buffer initial-mod-div 
                len len ;;meter
                queue initial-queue
                queue-buffer initial-queue
                new-user-data nil
                last-tick  (.GetCurrentTimeSamples csound Csound) ;; (.-beat Abletonlink)
                stop? false
                last-fx initial-fx]
        (let [{:keys [pause kill stop? dur input-messages meter fx len]
               :or {;;input-messages input-messages meter meter stop? stop?
                    len len}}  new-user-data
              [queue-buffer mod-div-buffer] (if kill
                                              [nil nil]
                                              (if dur
                                                [(create-event-queue dur input-messages)
                                                 (calc-mod-div (or len meter) dur)]
                                                [queue-buffer mod-div-buffer]))
              _ (when (and (not kill)
                           new-user-data
                           (or (not= last-fx fx) (nil? fx))
                           ;;(fn? (:recompile-fn new-user-data))
                           ) 
                  (println "recompileing fx-changes...")
                  ;; (prn new-user-data)
                  ((:recompile-fn new-user-data))
                  )
              ;;_ (when new-user-data (prn "END OF CALC"))
              new-user-data nil]
          ;; (println (str "Mod-div: "  mod-div (count queue-buffer)))
          (if kill
            (swap! pattern-registry dissoc pattern-name user-input-channel)
            (if-let [next-event (peek queue)] 
              (let [timestamp (calculate-timestamp
                               last-tick
                               mod-div (first next-event))
                    wait-chn (chan)]
                (loop []
                  (if (<= timestamp (.GetCurrentTimeSamples csound Csound))
                    (do (.InputMessage csound Csound (second next-event))
                        (put! wait-chn true))
                    (do (<! (timeout 1))
                        (recur))))                
                (when (<! wait-chn) 
                  (recur (inc index)
                         (inc a-index)
                         mod-div
                         mod-div-buffer
                         ;; meter
                         len
                         (pop queue)
                         queue-buffer
                         (async/poll! user-input-channel)
                         (.GetCurrentTimeSamples csound Csound)
                         stop?
                         fx)))
              (recur 0
                     (inc a-index)
                     mod-div-buffer
                     mod-div-buffer
                     ;; meter
                     len
                     queue-buffer
                     queue-buffer
                     (if stop?
                       (<! user-input-channel)
                       (async/poll! user-input-channel))
                     (.GetCurrentTimeSamples csound Csound)
                     false
                     fx))))))))


(defn pat [pattern-name instr env]
  (let [instr (if (vector? instr)
                instr (apply instr (mapcat identity env)))]
    (when-not (or (empty? env) (nil? env))
      (pattern-loop-queue (merge (nth instr 2)
                                 (ast-input-messages-builder
                                  (assoc env :pattern-name (name pattern-name)) instr)
                                 {:pattern-name (str pattern-name)
                                  :recompile-fn (nth instr 4)})))))

(comment 
  (pat :melody1 (panaeolus.instruments.tr808/low_conga)
       #_(seq [1 1 1 1:2] 2)

       (panaeolus.macros/->  (assoc 
                              :dur [1 1 1 0.25 0.125 0.125 0.5])
                             (assoc :kill true)
                             ))

  (pattern-loop-queue
   (do (panaeolus.instruments.tr808/low_conga)
       (-> {:dur [1 1 1 -0.25 0.25 0.5]
            :pattern-name :abc
            :input-messages "i 3 0 0.1 -8 100"
            :meter 4
            :kill true
            }
           (assoc :dur [1 1 1 0.125 0.125 0.25 0.5]))))

  (bpm! 361)
  (pattern-loop-queue
   {:dur [1 1 1 0.5 0.5]
    :pattern-name :c
    :input-messages "i 2 0 0.01 2"
    :meter 0
    :kill true
    })

  (.EvalCode csound Csound "giSigmoid ftgen  0, 0, 1024, 19, 0.5, 0.5, 270, 0.5")
  (.ScoreEvent csound Csound "i" #js [2 0 1])
  (.InputMessage csound Csound "i 2 0 1")
  (dur->event-queue [1 1 1 1] #queue []))

(go (let [event-c (chan)]
      (>! poll-channel [0 300 event-c])
      (when (<! event-c)
        (.InputMessage csound Csound "i 2 0 1"))))


