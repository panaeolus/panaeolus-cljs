instr 1
  iamp = ampdbfs(p4)
  idur = p3
  ifreq = p5
  isample = p6
  istretch = p7
  ilen ftlen isample
  isr ftsr isample
  p3 = istretch * (ilen/isr)
  
  aL paulstretch istretch, 0.2, isample
  
  aenv  linseg 0, 0.0005, 1, p3 - 0.0395, 1, 0.02, 0, 0.01, 0
  aL =  aL*aenv*0.1
  aR =  aL

  outs aL, aR
endin
