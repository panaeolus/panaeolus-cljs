instr 1
  iamp = ampdb(p4)
  kenv expon iamp, p3, 0.1
  ifreq = p5
  apulse mpulse kenv, 1/ifreq
  apulse moogladder2 apulse, expon:k(sr/2, p6, ifreq), 0.2
  aR = apulse
  aL = apulse
  outs aR, aL
endin
