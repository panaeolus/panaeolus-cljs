instr 1
  iCarAmp = ampdb(p4)
  ibasefreq = p5
  index1 = p6
  index2 = p7
  index3 = p8
  iratio1 = p9
  iratio2 = p10
  iratio3 = p11
  icar = p12
  
  iporttime = .05
  kindex1 linseg index1*0.1, p3*0.5, index1,p3*0.5,index1*0.1
  kindex2 linseg index2*0.1, p3*0.5, index2, p3*0.5, index2*0.1
  kindex3 linseg index3*0.1, p3*0.5, index3, p3*0.5, index3*0.1
  kbasefreq line ibasefreq*random:i(0.9,0.98), p3*0.1, ibasefreq
  kCarAmp expon iCarAmp, p3, iCarAmp*0.1
  
  aModPhase1 phasor ibasefreq * iratio1
  aModulator1 tablei aModPhase1,giSine,1,0,1
  aModulator1 = aModulator1*kindex1

  aModPhase2 phasor ibasefreq * iratio2
  aModPhase2 = aModPhase2 + aModulator1
  aModulator2 tablei aModPhase2,giSine,1,0,1
  aModulator2 = aModulator2 * kindex2

  aModPhase3 phasor ibasefreq * iratio3
  aModPhase3 = aModPhase3 + aModulator2
  aModulator3 tablei aModPhase3,giSine,1,0,1
  aModulator3 = aModulator3 * kindex3

  aCarrPhase phasor ibasefreq * icar
  aCarrPhase = aCarrPhase + aModulator3 
  aCarrier tablei aCarrPhase,giSine,1,0,1
  aDeclick linseg 0, 0.02, 1, p3 - 0.05, 1, 0.02, 0, 0.01, 0
  aCarrier = aCarrier*kCarAmp

  aoutL = aCarrier*aDeclick*0.3
  aoutR = aCarrier*aDeclick*0.3
  outs aoutL, aoutR
endin
