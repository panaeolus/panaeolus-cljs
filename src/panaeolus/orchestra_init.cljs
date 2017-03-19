(ns panaeolus.orchestra-init
  (:require [macchiato.fs :as fs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INITIAL CSOUND ORCHESTRA ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def ^:private orc-init-constants
  "
  nchnls=2\n
  0dbfs=1\n
  sr=44100\n
  ksmps=1\n
  " )

(def ^:private orc-init-tables
  (str
   (fs/slurp "src/panaeolus/csound/tables/tables.orc") "\n"
   (fs/slurp "src/panaeolus/csound/tables/oscil_bank.orc") "\n"
   (fs/slurp "src/panaeolus/csound/tables/hammer.orc")))

(def ^:private orc-init-instr-1
  "
  ;;alwayson 1
  ;; zakinit 4, 1 
  instr 1
  ;; setksmps 1
  endin
  ")

(def ^:private orc-init-udo
  (fs/slurp "src/panaeolus/csound/udo/Partial.udo"))

(def ^:private orc-init-fx
  (str
   (fs/slurp "src/panaeolus/csound/fx/flanger.udo")
   (fs/slurp "src/panaeolus/csound/fx/lofi.udo")))

(def ^:private orc-init-bottom
  "
  alwayson 10000
  instr 10000
  aMasterLeft chnget \"OutL\"
  aMasterRight chnget \"OutR\"
  outs aMasterLeft,\\ 
       aMasterRight
  ;;generating a different filename each time csound renders
  itim     date
  Stim     dates     itim
  Syear    strsub    Stim, 20, 24
  Smonth   strsub    Stim, 4, 7
  Sday     strsub    Stim, 8, 10
  iday     strtod    Sday
  Shor     strsub    Stim, 11, 13
  Smin     strsub    Stim, 14, 16
  Ssec     strsub    Stim, 17, 19
  Sfilnam  sprintf  \"/home/hlolli/Music/%s_%s_%02d_%s_%s_%s.wav\", Syear, Smonth, iday, Shor,Smin, Ssec
  ;;  fout Sfilnam, 4, aMasterLeft, aMasterRight
  chnclear \"OutL\"
  chnclear \"OutR\" 
  endin
  ")


(def orc-init
  (str orc-init-constants
       orc-init-tables
       orc-init-instr-1 "\n"
       orc-init-udo "\n"
       orc-init-fx "\n"
       orc-init-bottom))
