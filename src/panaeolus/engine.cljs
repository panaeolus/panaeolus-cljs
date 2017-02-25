(ns panaeolus.engine
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

(def metro-channel (chan))

(def poll-channel (chan))

(def pattern-registry (atom {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; METRONOME CLOCK CONTROLLER ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def BEAT (.GetKsmps csound Csound))

(def TICKS-PER-SECOND (atom BEAT))

(defn bpm! [bpm]
  (let [bps (/ 60 bpm)
        tps (int (* bps BEAT))]
    (reset! TICKS-PER-SECOND tps)))


;; (.GetControlChannel csound Csound "metro" nil)

(defonce performance-loop
  (atom (.PerformKsmpsAsync
         csound Csound
         (fn [] (go (>! metro-channel true)))
         (fn [] (.Stop csound Csound)))))


(def main-loop
  (atom (go-loop [event-poll #queue []
                  poll #queue []
                  tick 0]
          (let [[e-poll poll] (if (peek poll)
                                [(into event-poll poll)
                                 #queue []]
                                [event-poll #queue []])]
            (when (<! metro-channel)
              (if-not (empty? e-poll)
                (recur
                 (reduce
                  (fn [init v] 
                    (let [divided (mod tick (second v))]
                      (if (= divided (first v))
                        (do
                          (go (>! (nth v 2) tick))
                          init)
                        (conj init v))))
                  []
                  e-poll)
                 (conj poll (async/poll! poll-channel))
                 (inc tick))
                (recur
                 ;; (inc ti)
                 e-poll
                 (conj poll (async/poll! poll-channel))
                 (inc tick))))))))

(comment 
  (go (js/console.log (<! metro-channel)))

  (go (let [event-c (chan 1)]
        (>! poll-channel [0 300 event-c])
        (when (<! event-c)
          (.InputMessage csound Csound "i 2 0 1"))))

  (.EvalCode csound Csound
             "instr 2\nasig poscil 0.1, (100 + rnd(480))\nouts asig,asig\nendin"))



