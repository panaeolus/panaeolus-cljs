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
    (<! (timeout 10))
    (.Reset csound Csound)
    (.Cleanup csound Csound)))

(.SetOption csound Csound "-odac")
(.SetOption csound Csound "-+rtaudio=alsa")
(.CompileOrc csound Csound orc-init)
(.Start csound Csound)
(.PerformAsync csound Csound (fn [] (.Stop csound Csound)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GLOBAL CHANNELS AND ATOMS ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def metro-channel (chan (async/sliding-buffer 1)))

(def poll-channel (chan (async/sliding-buffer 2048)))

(def pattern-registry (atom {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; METRONOME CLOCK CONTROLLER ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; (.-beat Abletonlink)
;;(.stopUpdate Abletonlink)
(.startUpdate Abletonlink 10 (fn [beat phase bpm]
                               (go (async/offer! metro-channel beat))))

(def TICKS-PER-SECOND 256)

(defn bpm! [bpm]
  (set! (.-bpm Abletonlink) bpm))

;; (.GetControlChannel csound Csound "metro" nil)

;; (defonce performance-loop
;;   (.PerformKsmpsAsync
;;    csound Csound
;;    (fn [] (go (>! metro-channel true)))
;;    (fn [] (.Stop csound Csound))))

(comment 
  (def q (new PriorityQueue))
  (.enqueue q 3 "c")
  (.enqueue q 1 "b")
  (.enqueue q 1 "a")
  (.peekKey q)
  (.dequeue q)
  (.getValues q) (.isEmpty q))



(def main-loop
  (let [priority-queue (new PriorityQueue)]
    (go-loop [new-events #queue []]
      (let [new-events (if-not (empty? new-events)
                         (do (.enqueue priority-queue
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
              (while (>= time (.peekKey priority-queue))
                (let [dequeued-chan (.dequeue priority-queue)]
                  (go (>! dequeued-chan true))))
              (recur (if-let [poll (async/poll! poll-channel)]
                       (conj new-events poll) new-events)))))))))

(comment 
  (go (js/console.log (<! metro-channel)))

  (go (let [event-c (chan)]
        (>! poll-channel [1 300 event-c])
        (when (<! event-c)
          (.InputMessage csound Csound "i 2 0 1"))))

  (.EvalCode csound Csound
             "instr 2\nasig poscil 0.9, (100 + rnd(480))\nouts asig,asig\nendin"))



