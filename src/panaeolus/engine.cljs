(ns panaeolus.engine
  (:require [cljs.core.async :as async
             :refer [<! >! chan timeout take! put!]]
            [panaeolus.orchestra-init :refer [orc-init]]
            [goog.object :as o])
  (:require-macros [cljs.core.async.macros
                    :refer [go go-loop]])
  (:import [goog.structs PriorityQueue]))

(declare csound)

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
(.InputMessage csound Csound "i 10000 0 99999999999")
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GLOBAL CHANNELS AND ATOMS ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def metro-channel (chan (async/sliding-buffer 1)))

(def poll-channel (chan (async/sliding-buffer 1024)))

(def pattern-registry (atom {:forever #{}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; METRONOME CLOCK CONTROLLER ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def bpm! nil)


;;;;;;;;;;;;;;;;;;;
;; Record audio ;;;
;;;;;;;;;;;;;;;;;;;

(def panaeolus-is-recording? (atom false))

(defn record! []
  (if @panaeolus-is-recording?
    (do (.InputMessage csound Csound "i 9999 0 1 0")
        (println "\nRecording stopped!\n")
        (reset! panaeolus-is-recording? false))
    (do (.InputMessage csound Csound "i 9999 0 1 1")
        (println "\nRecording started....\n")
        (reset! panaeolus-is-recording? true))))
