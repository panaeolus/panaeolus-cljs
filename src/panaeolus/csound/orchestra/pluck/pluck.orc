instr 1
  iamp = ampdb(p3)
  ifreq = p5
  irough = p6
  apluck pluck iamp, ifreq, ifreq/2, 0, 3, irough
  apluckFollow follow2 apluck, p3*0.1, p3*0.5
  atri poscil iamp, ifreq*1.0015, giTriangle
  asin poscil iamp, ifreq/2, giSine
  aclick lowpass2 mpulse:a(100,0), line:k(8000,p3,2000),2.5
  asig = (apluck + atri + asin)*0.3*apluckFollow
  afilt moogvcf2 asig, aclick,line:k(2.5,p3,0)
  asig lowpass2 asig, line:k(ifreq*100,ifreq*10,0.25),0.98
  aL,aR hilbert asig
  if p3 < 0 then
    kenv linsegr 1, .05, 0.5, 5, 0
    asigL = aL * kenv
    asigR = aR * kenv
  else
    asigL linenr aL/3,0.01,p3*2,0.1
    asigR linenr aR/3,0.01,p3*2,0.1
  endif
  outs asigL, asigR
endin
