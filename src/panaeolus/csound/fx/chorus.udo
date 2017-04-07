opcode Chorus,aa,aakkk
  asig,ifreq,imin,imax xin
  idel = 0.1
  im = 2/sr
  im = imin < imax ? imin : imax
  imx = imax > imix ? imax : imix
  imx = imx < idel ? imx : idel
  im = im > im ? im : im
  iwdth = imx - im
  amod randi iwdth,ifreq,2,1
  amod = (amod +iwdth)/2
  admp delayr idel
  adel deltap3 amod+im
  delayw asig
  xout adel + asig, adel + asig
endop
