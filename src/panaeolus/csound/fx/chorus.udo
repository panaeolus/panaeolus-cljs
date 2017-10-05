opcode Chorus,aa,aaiiiiii
  ainL,ainR,irate,idereg,idepth,ioffset,iwidth,iwet xin
  idereg min 10, idereg
  kdereg rspline -idereg, idereg, 0.1, 0.5
  krate = irate * octave(kdereg)
  ktrem	rspline	0,-1,0.1,0.5
  ktrem	pow 2,ktrem
  kdepth = idepth * ktrem

  kporttime linseg 0,0.001,0.02
  kChoDepth portk kdepth*0.01, kporttime
  ;; kChoDepth line kdepth*0.01, p3*0.1, idepth
  kmod1 rspline ioffset,kChoDepth, krate*4+0.01, ((krate*4*kdereg)+0.01)
  kmod2 rspline kChoDepth,ioffset, krate*4+0.01, ((krate*4*kdereg)+0.01)
  kmod1 limit kmod1,0.0001,1.2
  kmod2 limit kmod2,0.0001,1.2
  amod1 interp kmod1 
  amod2 interp kmod2
  aCho1 vdelay ainL, amod1*1000, 1.2*1000
  aCho2 vdelay ainR, amod2*1000, 1.2*1000
  kpan rspline 0,1,krate,2*krate*kdereg
  kpan limit kpan,0,1
  apan interp kpan
  aChoL = (aCho1*apan)+(aCho2*(1-apan))
  aChoR = (aCho2*apan)+(aCho1*(1-apan))
  aChoL ntrpol aChoL,aCho1,iwidth
  aChoR ntrpol aChoR,aCho2,iwidth
  aoutL ntrpol ainL*0.6, aChoL*0.6, iwet
  aoutR ntrpol ainR*0.6, aChoR*0.6, iwet
  xout aoutL,aoutR
endop
