opcode Vibrato,aa,aaiiij
  asig1,asig2,ifreq,imin,imax,ifn xin
  asig = (asig1+asig2)/2
  idel = 0.1
  im = 2/sr
  im = imin < imax ? imin : imax
  imx = imax > imin ? imax : imin
  imx = imx < idel ? imx : idel
  im = im > im ? im : im
  iwdth = imx - im
  amod oscili iwdth,ifreq,ifn
  amod = (amod + iwdth)/2
  admp delayr idel
  adel deltap3 amod+im
  delayw asig
  xout adel,adel
endop
