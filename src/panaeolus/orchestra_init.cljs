(ns panaeolus.orchestra-init
  (:require [macchiato.fs :as fs])
  (:import [goog.string format]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INITIAL CSOUND ORCHESTRA ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def ^:private orc-init-constants
  "
  nchnls=2\n
  0dbfs=1\n
  sr=44100\n
  ksmps=128\n
  " )
(def expand-home-dir (js/require "expand-home-dir"))

(def ^:private orc-init-globals
  (format "gkRecording init 0 \n
           gSRecordBaseLocation init \"%s\"\n
           gSRecordLocation init \"\"\n
           gkPanaeolusBPM init 60 \n
" (expand-home-dir "~/Music")))


(def ^:private orc-init-tables
  (str
   (fs/slurp "src/panaeolus/csound/tables/tables.orc") "\n"
   (fs/slurp "src/panaeolus/csound/tables/oscil_bank.orc") "\n"
   (fs/slurp "src/panaeolus/csound/tables/hammer.orc") "\n"
   (fs/slurp "src/panaeolus/csound/tables/scanned.orc")))

(def ^:private orc-init-instr-1
  "instr 1
  setksmps 1
  kLastPhase init 0
  kTicks     init 0
  iTickResolution = 1024
  kPhase phasor iTickResolution*(gkPanaeolusBPM/60)
  if kPhase < kLastPhase then
  kTicks += 1
  chnset kTicks, \"panaeolusClock\"
  ;;printk 1, kTicks
  endif
  kLastPhase = kPhase
  endin
  ")

#_(def ^:private orc-init-fx
    (str
     (fs/slurp "src/panaeolus/csound/fx/pitch_shifter.udo") "\n"
     (fs/slurp "src/panaeolus/csound/fx/pitch_shifter_2.udo") "\n"))

(def ^:private orc-init-bottom
  "
  ;; Record controller
  ;; instr  9999
  ;; gkRecording = p4
  ;; if gkRecording > 0 then
  ;;   ;;generating a different filename each time csound renders
  ;;   itim     date
  ;;   Stim     dates     itim
  ;;   Syear    strsub    Stim, 20, 24
  ;;   Smonth   strsub    Stim, 4, 7
  ;;   Sday     strsub    Stim, 8, 10
  ;;   iday     strtod    Sday
  ;;   Shor     strsub    Stim, 11, 13
  ;;   Smin     strsub    Stim, 14, 16
  ;;   Ssec     strsub    Stim, 17, 19
  ;;   gSRecordLocation sprintf  \"%s/%s_%s_%02d_%s_%s_%s.wav\",\\
  ;;                              gSRecordBaseLocation,\\ 
  ;;                              Syear, Smonth, iday, Shor,Smin, Ssec
  ;;   SRecorder = {{ 
  ;;   instr 10001
  ;;   aL, aR monitor
  ;;   fout gSRecordLocation, 4, aL, aR
  ;;   if gkRecording == 0 then
  ;;     turnoff
  ;;   endif
  ;;   endin
  ;;   }}
  ;;   kactive active 10001
  ;;   iactive active 10001
  ;;   if (kactive == 1) || (iactive == 1)  then
  ;;   else
  ;;     iresult compilestr SRecorder
  ;;     event \"i\", 10001, 0, 3600*24
  ;;   endif
  ;; endif
  ;; endin

  ;; Master output
  instr 10000
  aMasterLeft chnget \"OutL\"
  aMasterRight chnget \"OutR\"
  aReverbLeft chnget \"RvbL\"
  aReverbRight chnget \"RvbR\"
  
  aRvbLeft, aRvbRight reverbsc aReverbLeft, aReverbRight, 0.85, 12000, sr, 0.5, 0

  outs aMasterLeft+aRvbLeft,\\
       aMasterRight+aRvbRight

  chnclear \"OutL\"
  chnclear \"OutR\" 
  chnclear \"RvbL\"
  chnclear \"RvbR\" 

  endin
  ")

(def orc-init
  (str orc-init-constants "\n"
       orc-init-tables "\n"
       orc-init-globals "\n"
       orc-init-instr-1 "\n"
       ;; NOTE GERA PITCH SHIFT FX!!
       ;; orc-init-fx "\n"
       orc-init-bottom "\n"
       ))
