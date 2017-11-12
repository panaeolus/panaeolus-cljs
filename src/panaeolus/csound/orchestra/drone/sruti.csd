instr 1 
  gkampSruti = ampdb(p4)/9
  gkbaseSruti = p5

  gknumSruti = max:i(p6, 1)
  gkdenSruti = p7
  gkrissetSruti = p8
  gkbbrate = p9
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
  giSrutiActive = 1
  
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
  ;; kpanner rspline -1, 1, 0.001, 2
  ;; a1L, a1R pan2 a1, kpanner

  ;; create Risset otput
  ;; krismix = 1
  aout sum a2, a3, a4, a5, a6, a7, a8, a9

  ;; create binaural beating otput

  a3L,a3R binauralize aout, gkfreqSruti, gkbbrate

  ; combine and send to global otput channels

  outs a3L, a3R
endin
}}
kactive active "SrutiDrone"
iactive active "SrutiDrone"
if (kactive == 1) || (iactive == 1) || ((giSrutiActive == 1)) then
else
iresult compilestr SAudioInstr
event "i", "SrutiDrone", 0, 3600*24
endif
endin
