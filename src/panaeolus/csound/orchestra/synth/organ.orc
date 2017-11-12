instr 1
  ap[] init 8
  ipf[] fillarray 440,480,590,610,700,850,912,990
  ipa[] fillarray 0.8, 0.9, 0.3,0.7,0.6,0.5,0.1,0.2
  kt= timeinsts()/p3
  iamp = ampdbfs(p4)
  ifreq = p5/440
  ap[0] Partial ipa[0],ipf[0]*ifreq,kt,giAdditive1,giAdditive2
  ap[1] Partial ipa[1],ipf[0]*ifreq,1.1*kt,giAdditive1,giAdditive2
  ap[2] Partial ipa[2],ipf[0]*ifreq,1.2*kt,giAdditive1,giAdditive2
  ap[3] Partial ipa[3],ipf[0]*ifreq,1.3*kt,giAdditive1,giAdditive2
  ap[4] Partial ipa[4],ipf[0]*ifreq,1.4*kt,giAdditive1,giAdditive2
  ap[5] Partial ipa[5],ipf[0]*ifreq,1.5*kt,giAdditive1,giAdditive2
  ap[6] Partial ipa[6],ipf[0]*ifreq,1.6*kt,giAdditive1,giAdditive2
  ap[7] Partial ipa[7],ipf[0]*ifreq,1.7*kt,giAdditive1,giAdditive2
  kcnt = 0
  amix = 0
  while kcnt < 8 do
    amix += ap[kcnt]
    kcnt += 1
  od
  asig linen iamp*amix*0dbfs/10,0.01,p3,0.01
  asig *= 0.25
  outs asig,asig
endin
