;; Assymetric FM UDO
;; Inspired by opcode in Victor Lazzarini's book Csound - Music and Computation System.

instr 1
  iamp=ampdbfs(p4)
  ifreq=p5
  ifm = p6 
  ifn = giExp2
  indx = p7
  iR = 1
  indx = indx*(iR+1/iR)*0.5
  indx2 = indx*(iR-1/iR)
  afm oscili indx/(2*$M_PI),ifm 
  aph phasor ifreq
  afc tablei aph+afm,ifn,1,0,1
  amod oscili indx2, ifm, -1, 0.25
  aexp tablei -(amod-abs(indx2))/1, ifn, 1
  asig = aexp*iamp*afc*0.1
  outs asig, asig
endin

