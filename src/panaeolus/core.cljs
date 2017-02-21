(ns panaeolus.core
  (:require [cljs.core.async :as async
             :refer [<! >! chan timeout]]
            [panaeolus.orchestra-init :refer [orc-init-top]])
  (:require-macros [cljs.core.async.macros
                    :refer [go go-loop]]))



(declare csound)

(if-not csound
  (do (def csound ((js* "require") "csound-api"))
      (def Csound (.Create csound)))
  (do
    (.InputMessage csound Csound "e")
    (<! (timeout 10))
    (.Reset csound Csound)
    (.Cleanup csound Csound)))

(.SetOption csound Csound "-odac")
(.SetOption csound Csound "-+rtaudio=alsa")
(.CompileOrc csound Csound (str orc-init-top))
(.Start csound Csound)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GLOBAL CHANNELS AND ATOMS ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def metro-channel (chan (async/sliding-buffer 2048)))

(def poll-channel (chan 1))

(def last-tick (atom 0))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; METRONOME CLOCK CONTROLLER ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; NOTE! Beat is always kr/4
;; Therefore maximum bpm is 240bpm

(defn bpm! [bpm]
  (let [bpm (cond
              (< bpm 0) 0
              (> bpm 240) 240
              :else bpm)
        tp4 (int (/ (.GetKr csound Csound) 4))
        ;; THIS ABOVE MAY RESULT IN UNSYNCHRONIZED
        ;; METRO CLOCKS!
        spb (/ 60.0 bpm)
        tpm (/ tp4 spb)]
    (.SetControlChannel
     csound Csound "metro" tpm)))

(bpm! 160)

;; (.GetControlChannel csound Csound "tickcnt" nil)

(defonce performance-loop
  (.PerformKsmpsAsync csound Csound
                      (fn [] (go (>! metro-channel
                                     (.GetControlChannel
                                      csound Csound "tickcnt" nil))))
                      (fn [] (.Stop csound Csound))))


(defonce event-manager
  (go-loop [event-poll []
            poll []]
    (let [[e-poll poll] (if (peek poll)
                          [(into event-poll poll)
                           []]
                          [event-poll []])]
      (when-let [tick (<! metro-channel)]
        (if-not (empty? e-poll)
          (recur
           (reduce
            (fn [init v]
              (if (= (first v)
                     (mod tick (second v)))
                (do
                  (go (>! (nth v 2) tick))
                  init)
                (conj init v)))
            []
            e-poll)
           (conj poll (async/poll! poll-channel)))
          (recur
           ;; (inc ti)
           e-poll
           (conj poll (async/poll! poll-channel))))))))

(go (js/console.log (<! metro-channel)))

(go (let [event-c (chan 1)]
      (>! poll-channel [0 300 event-c])
      (when (<! event-c)
        (.InputMessage csound Csound "i 2 0 1"))))

(.CompileOrc csound Csound
             "instr 2\nasig poscil 0.9, 200\nouts asig,asig\nendin")




