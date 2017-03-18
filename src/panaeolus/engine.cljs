(ns panaeolus.engine
  (:require [cljs.core.async :as async
             :refer [<! >! chan timeout take! put!]]
            [panaeolus.orchestra-init :refer [orc-init]]
            [goog.object :as o])
  (:require-macros [cljs.core.async.macros
                    :refer [go go-loop]])
  (:import [goog.structs PriorityQueue]))


(declare csound)

(def abletonlink (js/require "abletonlink"))

(def Abletonlink (new abletonlink))


(if-not csound
  (do (def csound (js/require "csound-api"))
      (def Csound (.Create csound)))
  (do
    (.InputMessage csound Csound "e")
    ;; (<! (timeout 10))
    (.Reset csound Csound)
    (.Cleanup csound Csound)))

(.SetOption csound Csound "-odac")
;; (.SetOption csound Csound "-+rtaudio=alsa")
(.CompileOrc csound Csound orc-init)
(.Start csound Csound)
;; (.PerformAsync csound Csound (fn [] (.Stop csound Csound)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GLOBAL CHANNELS AND ATOMS ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def metro-channel (chan (async/sliding-buffer 1)))

(def poll-channel (chan (async/sliding-buffer 1024)))

(def pattern-registry (atom {:forever #{}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; METRONOME CLOCK CONTROLLER ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def priority-queue (new PriorityQueue))

;; (.GetKsmps csound Csound)
;; (.-beat Abletonlink)
;;(.stopUpdate Abletonlink)
#_(.startUpdate Abletonlink 1
                (fn [beat phase bpm]
                  (go (loop [] (when-let [poll (async/poll! poll-channel)]
                                 (.enqueue priority-queue (first poll) (second poll))
                                 (recur)))
                      #_(loop [] (when (>= beat (.peekKey priority-queue))
                                   (put! (.dequeue priority-queue) true)
                                   (recur)))
                      (while (>= beat (.peekKey priority-queue))
                        (let [dequeued-chan (.dequeue priority-queue)]
                          (put! dequeued-chan true))))
                  ;; (prn beat)
                  ;; (go (>! metro-channel beat))
                  ))

(.PerformKsmpsAsync csound Csound
                    (fn []
                      (let [t (.GetCurrentTimeSamples csound Csound)]
                        (go (loop [] (when-let [poll (async/poll! poll-channel)]
                                       (.enqueue priority-queue (first poll) (second poll))
                                       (recur)))
                            #_(loop [] (when (>= beat (.peekKey priority-queue))
                                         (put! (.dequeue priority-queue) true)
                                         (recur)))
                            (while (>= t (.peekKey priority-queue))
                              (let [dequeued-chan (.dequeue priority-queue)]
                                (put! dequeued-chan true)))))
                      ;; (prn beat)
                      ;; (go (>! metro-channel beat))
                      ) 
                    (fn [] (.Stop csound Csound)))

;; (def *SR* (.GetKsmps csound Csound))

(def bpm! nil)

#_(defn bpm! [bpm]
    (set! (.-bpm Abletonlink) bpm))


#_(def main-loop
    (let [priority-queue (new PriorityQueue)]
      (go-loop [new-events #queue []]
        (let [new-events (if-not (empty? new-events)
                           (do 
                             (.enqueue priority-queue
                                       (first (peek new-events))
                                       (second (peek new-events)))
                             (pop new-events))
                           new-events)]
          (when-let [time (<! metro-channel)] 
            #_(prn (.getKeys priority-queue)
                   (.getValues priority-queue))
            (if (.isEmpty priority-queue)
              (recur (if-let [poll (async/poll! poll-channel)]
                       (conj new-events poll) new-events))
              (do
                ;;(prn "time: " time ">=" (.peekKey priority-queue))
                (while (>= time (.peekKey priority-queue))
                  (let [dequeued-chan (.dequeue priority-queue)]
                    (go (>! dequeued-chan true))))
                (recur (if-let [poll (async/poll! poll-channel)]
                         (conj new-events poll)
                         new-events)))))))))

(comment 
  (go (js/console.log (<! metro-channel)))

  (go (let [event-c (chan)]
        (>! poll-channel [3000 event-c])
        (when (<! event-c)
          (.InputMessage csound Csound "i 2 0 1"))))

  (.EvalCode csound Csound
             "instr 2\nasig poscil 0.9, (100 + rnd(480))\nouts asig,asig\nendin"))



