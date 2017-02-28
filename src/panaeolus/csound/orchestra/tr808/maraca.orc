instr 1
  idur = 0.07 * p3
  iamp = ampdb(p4+6)
  p3 limit idur,0.1,10
  iHPF = 6000
  iLPF = 10000
  iBP1 = 0.4
  iDur1 = 0.014* p3
  iBP2 = 1
  iDur2 = 0.01 * p3
  iBP3 = 0.05
  aenv	expsega	iBP1,iDur1,iBP2,iDur2,iBP3
  anoise noise	0.75,0
  anoise buthp	anoise,iHPF
  anoise butlp	anoise,iLPF
  anoise = anoise*aenv*iamp
  aL,aR	pan2 anoise,0.5 - linrand:i(1)
  outs aL,aR
endin
