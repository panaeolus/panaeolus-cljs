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
 
  outs asig, asig
endin
