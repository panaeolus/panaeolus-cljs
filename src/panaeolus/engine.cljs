(ns panaeolus.engine
  (:require [cljs.core.async :as async
             :refer [<! >! chan timeout take! put!]]
            [panaeolus.orchestra-init :refer [orc-init]]
            [goog.object :as o])
  (:require-macros [cljs.core.async.macros
                    :refer [go go-loop]])
  (:import [goog.structs PriorityQueue]))


(declare csound)

;; (def abletonlink (js/require "abletonlink"))

;; (def Abletonlink (new abletonlink))


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
(.PerformAsync csound Csound (fn [] (.Stop csound Csound)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GLOBAL CHANNELS AND ATOMS ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def metro-channel (chan (async/sliding-buffer 1)))

(def poll-channel (chan (async/sliding-buffer 1024)))

(def pattern-registry (atom {:forever #{}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; METRONOME CLOCK CONTROLLER ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; (def FastPriorityQueue (js/require "fastpriorityqueue"))
;; (def priority-queue (new FastPriorityQueue (fn [a b] (< (first a) (first b)))))


(def bpm! nil)

#_(defn bpm! [bpm]
    (set! (.-bpm Abletonlink) bpm))



(comment 
  (go (js/console.log (<! metro-channel)))

  (go (let [event-c (chan)]
        (>! poll-channel [3000 event-c])
        (when (<! event-c)
          (.InputMessage csound Csound "i 1 0 1"))))

  
  (.EvalCode csound Csound  panaeolus.orchestra-init/orc-init-fx
             ;; "instr 2\nasig poscil 0.9, (100 + rnd(480))\nouts asig,asig\nendin"
             ))



