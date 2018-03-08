(ns panaeolus.engine
  (:require
   [cljs.core.async :as async :refer [<! >! chan timeout take! put!]]
   [clojure.string :as string]
   [csound-wasm.public :as csound]
   [csound-wasm.node :as csound-node]
   [panaeolus.orchestra-init :refer [orc-init] :as orchestra-init]
   ["fs" :as fs]
   ["path" :as path]
   ["expand-home-dir" :as expand-home-dir]
   ["abletonlink" :as abletonlink])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:import [goog.string format]))


(def session-filename
  (let [date (new js/Date)
        year (.getFullYear date)
        month (nth ["January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"]
                   (.getMonth date))
        day (.getDate date)
        hour (.getHours date)
        minute (.getMinutes date)]
    (when-not (fs/existsSync (expand-home-dir "~/.panaeolus"))
      (fs/mkdirSync (expand-home-dir "~/.panaeolus")))
    (when-not (fs/existsSync (path/join (expand-home-dir "~/.panaeolus") "recordings"))
      (fs/mkdirSync (path/join (expand-home-dir "~/.panaeolus") "recordings")))
    (->> (str "session-" year "-" month "-" day "-" hour ":" minute ".sco")
         (path/join (expand-home-dir "~/.panaeolus") "recordings"))))


(csound/start-realtime)

(csound/compile-orc
 "instr 1
  Str strget p4
  ;;ires compilestr Str
  prints Str
  endin")


;; (csound/get-score-time-sync nil)


(defn compile-orc [orc]
  (let [msg (str "\"" orc "\"")]
    ;; (println msg)
    (csound/compile-orc orc)
    ;; (csound/input-message (str "i 1 0 0 " msg))
    (fs/appendFileSync
     session-filename
     (str "i 1 " (csound/get-score-time-sync) " 0 " msg "\n"))))


(defn input-message [sco]
  (csound/input-message sco ;; (format sco "0")
                        )
  #_(fs/appendFileSync
     session-filename
     (format sco (csound/get-score-time-sync))))


(compile-orc orc-init)
(input-message "i 10000 0 9999999999")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GLOBAL CHANNELS AND ATOMS ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def metro-channel (chan (async/sliding-buffer 1)))

(def poll-channel (chan (async/sliding-buffer 1024)))

(def pattern-registry (atom {:forever #{}}))

(defn get-pattern-reg-state []
  (reverse (into '(:panaeolus-runtime)
                 (keys (dissoc @pattern-registry :forever)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; METRONOME CLOCK/Ableton Link ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def clock-source :link)

(def ableton-link (new abletonlink))

(defn ableton-link-enable []
  (.enable ableton-link))

(defn ableton-link-update []
  (.update ableton-link))

(defn ableton-link-set-beat [beat]
  (.setBeatForce ableton-link beat))

(defn ableton-link-get-beat []
  (.-beat ableton-link))

(defn ableton-link-get-phase []
  (.-phase ableton-link))

(defn ableton-link-set-bpm [bpm]
  (set! (.-bpm ableton-link) bpm))

(defn ableton-link-get-bpm []
  (.-bpm ableton-link))

(defn ableton-link-get-peers []
  (.getNumPeers ableton-link))

(defn bpm! [bpm]
  (ableton-link-set-bpm bpm)
  #_(if (= clock-source :link)
      (ableton-link-set-bpm bpm)
      (set-control-channel "gkPanaeolusBPM" bpm)))

(def ableton-clock-state
  (volatile! 0))

;; (.startUpdate ableton-link 1
;;               (fn [beat _ _] (vreset! ableton-clock-state beat)))


(comment
  (compile-orc "giExp2 ftgen 0, 0, 131072, \"exp\", 0, -50, 1")
  (compile-orc "
instr 2
asig = poscil:a(1000.9, 440)
outc asig, asig
endin
")
  
  (csound/input-message "i 2 0 1"))
