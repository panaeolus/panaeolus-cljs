instr 1	
  idur = p3 
  iamp = ampdbfs(p4)
  ifreq = p5
  icutoff = p6
  ifn =	giSawRev

  aosc	oscil iamp, ifreq, ifn, -1
  aosc2	oscil iamp, ifreq + 4, ifn, -1
  aosc3	oscil iamp, ifreq - 4, ifn, -1
  asig	= (aosc+aosc2+aosc3)*.133
  asig	butterlp asig, icutoff

  outs	asig, asig
endin
