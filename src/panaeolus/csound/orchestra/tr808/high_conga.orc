instr 1
  ifrq = 420
  p3 = 0.33*p3
  iamp = ampdb(p4+5)
  aenv transeg 0.7,1/ifrq,1,1,p3,-6,0.001
  afrq expsega ifrq*3,0.25/ifrq,ifrq,1,ifrq
  asig oscili -aenv*0.25,afrq,giSine
  asig	= asig*iamp
  aL,aR pan2 asig,0.5 - linrand:i(1)
  outs	aL,aR
endin
