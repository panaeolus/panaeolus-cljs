;;Electric priest from Tobias Enhus
;   strt 	dur 	form	pitch	p6;formant attack	p7; Amp attack P8;reverb p9: amp	
instr 1
  p3 = p3 + p7
  iamp = ampdbfs (p4)
  ifreq=p5
  k2 linseg 0, p3*.9, 0, p3*.1, 1  			; octaviation coefficient
  a1 oscil 7, .15,giSine
  a3 linen a1, (p3-p3*.05), p3, .2
  a2 oscil a3, 5,giSine

  a21 line 456, p6, 1030
  a5 linen 0.3*iamp, p7, p3, (p3*.1)			;amp envelope
  a11 fof a5,ifreq+a2, a21*(p8/100), k2, 200, .003, .017, .005, 30, giSine,giSigmoid, ifreq, 0, 1

  a31	line	0.12*iamp, p6, 0.20*iamp
  a32	line	2471, p6, 1370
  a6 linen a31, p7, p3, (p3*.1)
  a12 fof a6,ifreq+a2, a32*(p8/100), k2, 200, .003, .017, .005, 30, giSine,giSigmoid, ifreq, 0, 1

  a41 line 2813, p6, 3170
  a42 line 0.05*iamp, p6, 0.06*iamp
  a7 linen a42, p7, p3, (p3*.1)			;amp envelope
  a13 fof a7,ifreq+a2, a41*(p8/100), k2, 200, .003, .017, .005, 30, giSine,giSigmoid, ifreq, 0, 1

  a71 line 1347, p6, 1726
  a72 line 0.12*iamp, p6, 0.11*iamp
  a8 linen a71, p7, p3, (p3*.1)	
  a14 fof a8,ifreq+a2, a72*(p8/100), k2, 200, .003, .017, .005, 30, giSine,giSigmoid, ifreq, 0, 1

  a51 line 0.001*iamp, p6, 0.04*iamp
  a9 linen a51, p7, p3, (p3*.1)			;amp envelope
  a15 fof a51,ifreq+a2, 4177*(p8/100), k2, 200, .003, .017, .005, 30, giSine,giSigmoid, ifreq, 0, 1

  a61 line 0.001*iamp, p6, 0.18*iamp
  a10 linen a51, p7, p3, (p3*.1)			;amp envelope
  a16 fof a10,ifreq+a2,  428*(p8/100), k2, 200, .003, .017, .005, 30, giSine,giSigmoid, ifreq, 0, 1
  a7 = (a11 + a12 + a13 + a14 + a15 + a16) * iamp
  aenv    linseg 0, 0.02, 1, p3 - 0.05, 1, 0.02, 0, 0.01, 0
  asig = a7/100*aenv
  
  outs  asig, asig
endin

