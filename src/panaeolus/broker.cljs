(ns panaeolus.broker
  (:require
   [cljs.core.async :refer [<! >! put! chan timeout] :as async]
   [panaeolus.engine :refer [poll-channel pattern-registry bpm!] :as engine]
   [goog.string :as gstring]
   [panaeolus.score-parser :refer [ast-input-messages-builder]]
   ["libcsound" :as libcsound]
   [csound-wasm.public :as csound])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))



(def tick-resolution 1024)

(defn calc-mod-div [meter durations]
  (if (= engine/clock-source :link)
    (max 1 meter)
    (let [meter (if meter
                  (if (< 0 meter) meter 0) 0)
          bar-length meter
          summed-durs (apply + (map Math/abs durations))]
      (* tick-resolution
         (if (< 0 meter)
           (* bar-length
              (inc (quot (dec summed-durs) bar-length)))
           summed-durs)))))

;; (create-event-queue [0 1 2 3] [(fn [] :a) [(fn [] :c)]])

(defn calculate-timestamp [last-tick mod-div beat]
  (if (= engine/clock-source :link)
    (let [last-tick (Math/ceil last-tick)
          current-beat (max (mod last-tick mod-div) 0)
          delta (- beat current-beat)]
      (if (neg? delta)
        (+ beat last-tick (- mod-div current-beat))
        (+ last-tick delta)))
    
    (let [beat (* beat tick-resolution)
          current-beat (max (mod last-tick mod-div) 0)
          delta (- beat current-beat)]
      (if (neg? delta)
        (+ beat last-tick (- mod-div current-beat))
        (+ last-tick delta)))))

;; (create-event-queue 100.1 0 [0 1.5 -2 3 10] [:a :b :c :d])
;; (create-event-queue 100 0 [1 1 -1 1 1] [:a :b :c :d])

(defn create-event-queue [last-tick mod-div beats event-callbacks]
  (into #queue []
        (loop [beats (remove #(zero? %) beats)
               msg event-callbacks
               silence 0
               last-beat 0
               at []]
          (if (empty? beats)
            (map #(vector (calculate-timestamp last-tick mod-div (first %))
                          (second %)) at)
            (let [fbeat (first beats)]
              (recur (rest beats)
                     (if (neg? fbeat)
                       msg
                       (if (empty? msg)
                         (rest event-callbacks)
                         (rest msg)))
                     (if (neg? fbeat)
                       (+ silence (Math/abs fbeat))
                       0)
                     (if (neg? fbeat)
                       last-beat
                       fbeat)
                     (if (neg? fbeat)
                       at
                       (conj at [(+ last-beat
                                    silence
                                    (if (empty? at)
                                      0 (first (last at))))
                                 (or (first msg)
                                     (first event-callbacks))]))))))))

(defn- read-clock []
  (do (engine/ableton-link-update)
      (engine/ableton-link-get-beat))
  #_(if (= :link engine/clock-source)
      (do (engine/ableton-link-update)
          (engine/ableton-link-get-beat))
      (engine/get-control-channel "panaeolusClock")))


(defn pattern-loop-queue [env]
  (if-let [user-input-channel (get @pattern-registry (:pattern-name env))] 
    (put! user-input-channel env)
    (when-not (:kill env)
      (let [{:keys [dur pattern-name meter len input-messages]} env
            user-input-channel (chan 0)
            engine-poll-channel (chan)
            initial-queue (if (or (not (string? input-messages))
                                  (not (string? (first input-messages))))
                            (mapv #(create-event-queue dur %) input-messages)
                            (create-event-queue dur input-messages))
            ;; initial-queue (create-event-queue dur input-messages)
            initial-mod-div (calc-mod-div meter dur)
            initial-fx (:fx env)]
        ((get env :recompile-fn))
        (swap! pattern-registry assoc pattern-name user-input-channel)
        (go-loop [index 0
                  a-index 0
                  loop-cnt 0
                  mod-div initial-mod-div
                  mod-div-buffer initial-mod-div 
                  len len
                  queue (if (string? (first initial-queue))
                          initial-queue
                          (first initial-queue))
                  queue-buffer initial-queue
                  new-user-data nil
                  last-tick @engine/ableton-clock-state ;;(read-clock)
                  stop? false
                  cur-fx (or initial-fx "")
                  last-fx (or initial-fx "")]
          (let [{:keys [pause kill stop? dur input-messages meter fx]} new-user-data
                [queue-buffer mod-div-buffer] (if (zero? index)
                                                [queue-buffer mod-div-buffer]
                                                (if kill
                                                  [nil nil]
                                                  (if dur
                                                    [(if (or (not (string? input-messages))
                                                             (not (string? (first input-messages))))
                                                       (mapv #(create-event-queue dur %) input-messages)
                                                       (create-event-queue dur input-messages))
                                                     (calc-mod-div (or meter len) dur)]
                                                    [queue-buffer mod-div-buffer])))
                
                ;; _ (when new-user-data (prn "END OF CALC"))
                ;; new-user-data nil
                ]
            ;; (println (str "Mod-div: "  mod-div (count queue-buffer)))
            (if kill
              (swap! pattern-registry dissoc pattern-name user-input-channel)
              (if-let [next-event (peek queue)] 
                (let [timestamp (calculate-timestamp
                                 last-tick
                                 mod-div (first next-event))
                      wait-chn (chan)]
                  ;; (>! wait-chn (<= timestamp @engine/ableton-clock-state))
                  ;; (engine/input-message csound (second next-event))
                  (loop []
                    (if (<= timestamp (read-clock))
                      (.nextTick js/process
                                 #(do (engine/input-message (second next-event))
                                      (put! wait-chn true)))
                      (do (<! (timeout 0.1))
                          (recur))))                
                  (when (<! wait-chn)
                    (recur (inc index)
                           (inc a-index)
                           loop-cnt
                           mod-div
                           mod-div-buffer
                           ;; meter
                           len
                           (pop queue)
                           queue-buffer
                           (or (async/poll! user-input-channel)
                               new-user-data)
                           (read-clock)
                           ;; @engine/ableton-clock-state
                           stop?
                           (or fx cur-fx)
                           last-fx)))
                ;;;;;;;;;;;;;;;;;
                ;;; New round ;;;
                ;;;;;;;;;;;;;;;;;
                
                (do
                  (when (and (not kill)
                             new-user-data
                             (not= (str last-fx) (str cur-fx))) 
                    (println "recompileing fx-changes...")
                    ;; (prn new-user-data)
                    (go ((get new-user-data :recompile-fn))))
                  (recur 0
                         (inc a-index)
                         (inc loop-cnt)
                         mod-div-buffer 
                         mod-div-buffer
                         ;; meter
                         len
                         (if (string? (first queue-buffer))
                           queue-buffer
                           (nth queue-buffer (mod loop-cnt (count queue-buffer))))
                         queue-buffer
                         (if stop?
                           (<! user-input-channel)
                           (async/poll! user-input-channel))
                         ;; @engine/ableton-clock-state
                         (read-clock)
                         false
                         nil
                         (or cur-fx last-fx)))))))))))

(defn P [pattern-name instr env]
  (if (:kill env)
    (panaeolus.broker/pattern-loop-queue
     (assoc env :pattern-name (str pattern-name)))
    (let [instr (instr pattern-name)
          instr (if (vector? instr)
                  instr (apply instr (mapcat identity env)))] 
      (when-not (or (empty? env) (nil? env))
        (prn "HERE")
        (panaeolus.broker/pattern-loop-queue
         (merge (ast-input-messages-builder
                 (assoc env :pattern-name (name pattern-name)) instr)
                {:pattern-name (str pattern-name)
                 :recompile-fn (:recompile-fn (nth instr 2))}))))))

;; engine/input-message

(defn at-do [timestamp wait-chan callback]
  (go-loop []
    (if (<= timestamp (read-clock))
      (do (callback)
          (put! wait-chan true))
      (do (<! (timeout 2))
          (recur)))))

(defn unfold-env [init env]
  (reduce (fn [i v] (merge i (v i))) init env))

(P nil ((panaeolus.instruments.fof/priest :freq 200 :amp 10) :aa)
   ;; (fn [env] (assoc env :a 1))
   ;;(fn [env] (assoc env :a 2 :b 3))
   )

(defn P [pattern-name instr & env]
  (let [pattern-name (str pattern-name)
        instr (instr pattern-name)
        ;; recompile-fn (-> instr (nth 2) :recompile-fn)
        initial-tick (read-clock)
        env (unfold-env (first instr) env)
        ;; pattern-state
        #_(atom {:beats (take 40 (cycle [0.25]))
                 :events [(fn [event-state]
                            (let [t (read-clock)]
                              ((libcsound/cwrap "CsoundObj_inputMessage" "number" #js ["number" "string"])
                               @csound/csound-instance (str "i 1 0 0 \"" t "\n\"\n i 3 0 1 -12"))

                              ;;(engine/input-message (str "i 1 0 0 \"" t "\n\""))
                              (println t))
                            ;; (panaeolus.macros/demo (panaeolus.instruments.tr808/clap :amp -22))
                            ;;(println "TEST: " (:index event-state))
                            )]})
        ]
    #_(go-loop [pattern-queue (create-event-queue initial-tick
                                                  (or (:mod-div @pattern-state) 0)
                                                  (:beats @pattern-state)
                                                  (:events @pattern-state))
                index-counter [0 0]]
        ;; (prn (map first pattern-queue))
        (if (empty? pattern-queue)
          nil
          (let [[timestamp input-message-callback] (peek pattern-queue)
                [index counter] index-counter
                event-state (assoc @pattern-state
                                   :index index :counter counter
                                   :initial-tick initial-tick)
                wait-chan (chan)]
            (at-do timestamp wait-chan #(input-message-callback event-state))
            (<! wait-chan)
            (recur (pop pattern-queue)
                   [(if (= 1 (count pattern-queue))
                      0 (inc index))
                    (inc counter)]))))
    pattern-state))

(js/setInterval #(let [t (read-clock)]
                   ((libcsound/cwrap "CsoundObj_inputMessage" "number" #js ["number" "string"])
                    @csound/csound-instance (str "i 1 0 0 \"" t "\n\"\n i 3 0 1 -12"))

                   ;;(engine/input-message (str "i 1 0 0 \"" t "\n\""))
                   (println t)) 1000)


((panaeolus.instruments.fof/priest :freq 200 :amp 10) :aa)




;; (P "bla" (fn [_]) )


;; (peek #queue ["a" "b"])


;; ((panaeolus.instruments.tr808/low_conga  :dur 10 :amp 2 :fx a) :aa)

#_(P :a (panaeolus.instruments.tr808/low_conga  :amp 1)
     (panaeolus.algo.nseq/nseq {} [0 1 2 3 4]))

#_(defmacro P [pattern-name instr env]
    `(let [env# ~env         
           instr# ~(into instr (:pattern-name pattern-name))
           _# (prn "INSTR: " instr#)
           instr# (if (vector? instr#)
                    instr# (apply instr# (mapcat identity env#)))]
       ;; (prn "A: " (assoc env# :pattern-name ~(name pattern-name)))
       (when-not (or (empty? env#) (nil? env#))
         (pattern-loop-queue (merge (nth instr# 2)
                                    (panaeolus.orchestra-parser/ast-input-messages-builder
                                     (assoc env# :pattern-name ~(name pattern-name))
                                     instr#)
                                    {:pattern-name ~(str pattern-name)})))))

#_(panaeolus.broker$macros/P :melody1 (panaeolus.instruments.tr808/low_conga)
                             #_(seq [1 1 1 1:2] 2)

                             (panaeolus.macros/-> (assoc 
                                                   :dur [1 1 1 0.25 0.125 0.125 0.5])
                                                  (assoc :kill true)
                                                  ))

#_(defn pat [pattern-name instr env]
    (let [instr (if (vector? instr)
                  instr (apply instr (mapcat identity env)))]
      (when-not (or (empty? env) (nil? env))
        (pattern-loop-queue (merge (nth instr 2)
                                   (ast-input-messages-builder
                                    (assoc env :pattern-name (name pattern-name)) instr)
                                   {:pattern-name (str pattern-name)})))))

(comment 
  (pat :melody1 (panaeolus.instruments.tr808/low_conga)
       #_(seq [1 1 1 1:2] 2)
       {:dur [1 1 1 0.25 0.125 0.125 0.5]}
       #_(panaeolus.macros/-> (assoc 
                               :dur [1 1 1 0.25 0.125 0.125 0.5])
                              ;; (assoc :kill true)
                              ))

  (pattern-loop-queue
   (do (panaeolus.instruments.tr808/low_conga)
       (-> {:dur [1 1 1 -0.25 0.25 0.5]
            :pattern-name :abc
            ;; :input-messages "i 3 0 0.1 -8 100"
            :meter 4
            ;; :kill true
            }
           (assoc :dur [1 1 1 0.125 0.125 0.25 0.5]))))

  (bpm! 361)
  (pattern-loop-queue
   {:dur [1 1 1 0.5 0.5]
    :pattern-name :c
    :input-messages "i 2 0 0.01 2"
    :meter 0
    :kill true
    }))


