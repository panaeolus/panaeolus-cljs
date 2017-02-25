(ns panaeolus.orchestra-init)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INITIAL CSOUND ORCHESTRA ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(def orc-init-top
  (str
   "nchnls=2\n0dbfs=1\n
  sr=44100\n
  ksmps=32\n
  ;; kr=441\n
  alwayson 1\n
  ;;alwayson 10000\n
  instr 1\n
  zakinit 4, 1 \n
  ktickcnt init 0
  gkmetrofreq chnget \"metro\"
  kmetro metro gkmetrofreq
  if kmetro == 1 then
  chnset ktickcnt, \"tickcnt\"
  ktickcnt += 1
  endif
  endin\n
" ))


(def orc-init-bottom
  "
  instr 10000
  aMasterLeft zar 0
  aMasterRight zar 1
  aZakRvbL zar 2
  aZakRvbR zar 3
  kRoom chnget \"roomsize\"
  kMasterVolume chnget \"mastervolume\"
  aRvbL,aRvbR reverbsc aZakRvbL*0.3, aZakRvbR*0.3, kRoom, 7000
  outs (aMasterLeft+aRvbL)*kMasterVolume,\\ 
       (aMasterRight+aRvbR)*kMasterVolume
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
  Sfilnam  sprintf  \"~/Music/%s_%s_%02d_%s_%s_%s.wav\", Syear, Smonth, iday, Shor,Smin, Ssec
  zacl 0,4
  endin
  ")
