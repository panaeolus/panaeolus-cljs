(ns panaeolus.orchestra-init
  (:require ["fs" :as fs]
            ["expand-home-dir" :as expand-home-dir]
            [clojure.string :as string])
  (:import [goog.string format]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INITIAL CSOUND ORCHESTRA ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(def orc-init-globals
  (format "gkRecording init 0 \n
           gSRecordBaseLocation init \"%s\"\n
           gSRecordLocation init \"\"\n
           gkPanaeolusBPM init 130 \n
           giSrutiActive init 0
" (expand-home-dir "~/.panaeolus/wav")))


(def orc-init-tables
  (str
   (str (fs/readFileSync "src/panaeolus/csound/tables/tables.orc")) "\n"
   (str (fs/readFileSync "src/panaeolus/csound/tables/oscil_bank.orc")) "\n"
   (str (fs/readFileSync "src/panaeolus/csound/tables/hammer.orc")) "\n"
   (str (fs/readFileSync "src/panaeolus/csound/tables/scanned.orc")) "\n"))


(def orc-init-bottom
  "
  instr 10000
  aMasterLeft chnget \"OutL\"
  aMasterRight chnget \"OutR\"
  aReverbLeft chnget \"RvbL\"
  aReverbRight chnget \"RvbR\"
  
  aRvbLeft, aRvbRight reverbsc aReverbLeft, aReverbRight, 0.85, 12000, sr, 0.5, 0

  aMasterLeft  clip aMasterLeft,  0, 0.8
  aMasterRight clip aMasterRight, 0, 0.8
  aRvbLeft     clip aRvbLeft,     0, 0.8
  aRvbRight    clip aRvbRight,    0, 0.8

  outs aMasterLeft+aRvbLeft,\\
       aMasterRight+aRvbRight

  chnclear \"OutL\"
  chnclear \"OutR\" 
  chnclear \"RvbL\"
  chnclear \"RvbR\" 

  endin
  ")

(def orc-init
  (str orc-init-tables "\n"
       orc-init-globals "\n"
       ;; NOTE GERA PITCH SHIFT FX!!
       ;; orc-init-fx "\n"
       orc-init-bottom "\n"))
