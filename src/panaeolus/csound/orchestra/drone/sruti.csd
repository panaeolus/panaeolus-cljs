instr 1 
  gkampSruti = ampdb(p4)/9
  gkbaseSruti = p5

  gknumSruti = p6
  gkdenSruti = p7
  gkrissetSruti = p8
  ;; determine pitch
  gkfracSruti = gknumSruti/gkdenSruti
  gkfreqSruti = gkbaseSruti*gkfracSruti

  ; set up Risset effect
  SAudioInstr = {{
instr SrutiDrone
  
  itbl = giDrone
  koff = gkrissetSruti
  koff0 = ((gkdenSruti*2)/gknumSruti)*koff
  koff1	= koff0
  koff2	= 2*koff
  koff3	= 3*koff
  koff4	= 4*koff

  ;; envelope
  kenv linenr gkampSruti, 2, 3, 0.01
  ; generate primary tone
  a1 poscil3	kenv, gkfreqSruti, itbl

  ; generate Risset tones
  a2 poscil3	kenv, gkfreqSruti+koff1, itbl
  a3 poscil3	kenv, gkfreqSruti+koff2, itbl
  a4 poscil3	kenv, gkfreqSruti+koff3, itbl
  a5 poscil3	kenv, gkfreqSruti+koff4, itbl
  a6 poscil3	kenv, gkfreqSruti-koff1, itbl
  a7 poscil3	kenv, gkfreqSruti-koff2, itbl
  a8 poscil3	kenv, gkfreqSruti-koff3, itbl
  a9 poscil3	kenv, gkfreqSruti-koff4, itbl

  ; create simple otput (just the primary oscillator)
  kpanner rspline -1, 1, 0.001, 2
  a1L, a1R pan2 a1, kpanner

  ;; create Risset otput
  ;; krismix = 1
  aout		sum		a2, a3, a4, a5, a6, a7, a8, a9
  a2L,a2R pan2 aout, kpanner

  ;; create binaural beating otput
  ;; kbbmix = 0.01
  ;; kbbrate = 0.1
  ;; a3L,a3R binauralize a1*kbbmix, gkfreqSruti, kbbrate

  ; combine and send to global otput channels
  aL = a1L+a2L ;;+a3L
  aR = a1R+a2R ;;+a3R
  outs aL, aR
endin
}}
kactive active "SrutiDrone"
iactive active "SrutiDrone"
if (kactive == 1) || (iactive == 1)  then
else
iresult compilestr SAudioInstr
event "i", "SrutiDrone", 0, 3600*24
endif
endin
