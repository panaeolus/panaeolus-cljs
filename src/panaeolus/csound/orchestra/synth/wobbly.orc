instr 1
  klfo init 0
  a1 init 0
  a2 init 0
  idur = p3
  p3 = p3*0.9
  iamp = ampdb(p4)
  ifreq = p5
  idivision = p6
  ires = p7
  ; Oscillators
  a1 vco2 iamp, ifreq * 1.005, 0
  a2 vco2 iamp, ifreq * 0.495, 10
  a1 = a1 + a2
  
  ; LFO for wobble sound
  klfo oscil 1, idivision/idur, giTriangle
  ; Filter
  ibase = ifreq
  imod = ibase * 9
  ; aenv linsegr 1, p3, 1,0.25,0
  aenv linseg 0, 0.02, 1, p3 - 0.05, 1, 0.02, 0, 0.01, 0
  a1 moogladder a1, ibase + imod * klfo, ires
  
  aSig = a1*aenv
  ; Output
  outs aSig, aSig
endin
