instr 1
  iamp = ampdb(p4-10)
  kfreq = p5
  a1 scantable iamp, kfreq, giDrone, giHammerMass, giHammerStiff, giHammerDamp, giHammerVel
  if p3 < 0 then
    kenv linsegr 1, .05, 0.5, 5, 0
    asig = a1 * kenv
  else
    asig linenr a1/3,0.01,p3*2,0.1
  endif
  outs asig, asig
endin
