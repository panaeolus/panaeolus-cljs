instr 1
  idur = p3
  iamp = ampdb(p4)
  ifreq = p5
  kenv1 expseg ifreq*20, 0.01, 50, idur - 0.01, ifreq
  asig1 oscil3 1, kenv1, giSine
  kenv2 line 1, idur, 0
  asig1 = asig1 * kenv2
  asig2 gauss  0.3
  kenv5 expseg ifreq*18, 0.1, 50, idur - 0.1, ifreq
  asig2 tone asig2, kenv5
  amix  = asig1 + asig2 
  kenv5 expseg ifreq*4, 0.05, ifreq*1.1, idur - 0.05, ifreq
  amix rezzy amix, kenv5, 10
  kenv6 linseg ifreq*1.5, idur, ifreq
  aosc oscil3 0.2, kenv6, giSine
  kenv4 expsegr 1, idur, 0.1,0.1,0.0001
  amix = ( amix * 0.8 + aosc * 1.2 ) * kenv4 * iamp
  outs amix, amix
endin
