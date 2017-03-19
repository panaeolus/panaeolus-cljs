instr 1
  iamp = ampdb(p4+7)
  ifrq = 227
  p3 = 0.41*p3
  aenv transeg	0.7,1/ifrq,1,1,p3,-6,0.001
  afrq expsega	ifrq*3,0.25/ifrq,ifrq,1,ifrq
  asig oscili -aenv*0.25,afrq,giSine
  asig = asig*iamp
  aL,aR	pan2 asig,0.5 - linrand:i(1)
  outs aL,aR
  ; gitime3 = gitime2
  ; gitime2 = gitime1
  ; gitime1 timek
  ; printf_i "Inaccuracy %f s", 1,(gitime1 - gitime2) - (gitime2 - gitime3)
endin
