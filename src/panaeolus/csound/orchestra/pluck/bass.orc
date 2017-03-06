instr 1
  iamp = ampdb(p3)
  ifreq = p5
  irough = p6
  apluck pluck iamp, ifreq, ifreq/2, 0, 3, irough
  atri poscil iamp, ifreq, giTriangle
  asin poscil iamp, ifreq, giSine
  aclick lowpass2 mpulse:a(100,0), line:k(8000,p3,2000),2.5
  aenv expon 1, p3, 0.0001
  asig = (apluck + atri + asin)*aenv*0.3
  afilt moogvcf2 asig, aclick,line:k(2.5,p3,0)
  asig lowpass2 asig, line:k(ifreq*100,ifreq*10,0.25),0.98
  asig += afilt/10
  asig LoFi, asig, 2,0
  aL,aR hilbert asig  
  outs aR, aR
endin
