instr 1
  idur    =       p3
  kamp    =       ampdb(p4)
  ifreq   =       p5

  iAmpAttack = limit(idur*0.2, 0.01, 1)
  iAmpDecay = idur-iAmpAttack
  kAmpEnv linseg 0, iAmpAttack, 1, iAmpDecay, 0.0001
  iFreqEnvAttack = limit(idur*0.8, 0.01, 10)
  iFreqEnvDecay = idur - iFreqEnvAttack
  kFreqEnv expseg 1, iFreqEnvAttack, iFreqEnvDecay
  krand randomi ifreq/2, ifreq, 1
  aSaw1 vco2 kamp, ifreq*1.05, 2, sqrt(krand*0.06), sqrt(krand*0.6)
  aSaw2 vco2 kamp*0.9, ifreq, 2, sqrt(krand*0.07), sqrt(krand*0.7)
  aSaw3 vco2 kamp*0.7, ifreq*2, 2, sqrt(krand*0.08), sqrt(krand*0.8)
  aFilt vlowres (aSaw1+aSaw2+aSaw3)/24, 1, 0.9, 4, 10+sqrt(krand*2)
  if p3 < 0 then
    aDeclick linsegr 0, 0.02, 1, 1, 0
  else
    aDeclick linseg 0, 0.02, 1, p3 - 0.05, 1, 0.02, 0, 0.01, 0
  endif
  aFilt *= aDeclick*kFreqEnv
  outs  aFilt, aFilt
endin
