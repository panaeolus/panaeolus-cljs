instr 1

  iamp = ampdb(p4)
  imidi = p5
  ifmd = p6
  icps = cpsmidinn(imidi)
  
  iseedL = times:i()
  iroot_tbl = giMute122
  
  imidi limit imidi,22,58
  
  iwave = iroot_tbl+(imidi-22)
  kmvt	jspline	2,2,2

  aenv expsegr 0.00001, 0.01, 1, p3-0.01, 0.1, p3/4,0.00001
  aL oscbnk icps, 0, ifmd*icps, 0, 10, iseedL, 0, kmvt,\
  0, 0, 238, 0, 8000, 1, 1, 1, 1, -1, iwave, \
  giCos, giCos, gieqffn, gieqlfn, gieqqfn
  asig = aL*iamp*aenv

  kdepth linseg 0,0.1,0,0.1,1
  
  kFLFO lfo kdepth,4.1234,0
  kFEnv linsegr 0,0.001,2,p3,1,0.001,0
  kCFoct limit 4.5*kFEnv,4,14 ;;0 = p
  kCF = cpsoct(kCFoct)
  
  asig tonex asig, kCF,6 ;;p?
  
  outs asig, asig
endin
