opcode Flanger_stereo,aa,aaiiii ;MADE BY IAIN MCCURDY
  aL,aR,irate,idepth,ifback,ilfoshape xin
  if ilfoshape==1 then
    amod oscili idepth, irate, giParabola
  elseif ilfoshape==2 then
    amod oscili idepth, irate, giSine
  elseif ilfoshape==3 then
    amod oscili idepth, irate, giTriangle
  elseif ilfoshape==4 then
    amod randomi 0, idepth, irate,1
  else
    amod randomh 0,idepth,irate,1
  endif
  adelsigL flanger aL, amod, ifback , 1.2
  adelsigL dcblock adelsigL
  adelsigR flanger aR, amod, ifback , 1.2
  adelsigR dcblock adelsigR

  xout adelsigL,adelsigR
endop


opcode Flanger_mono,aa,aaiiii ;MADE BY IAIN MCCURDY
  aL,aR,irate,idepth,ifback,ilfoshape xin
  if ilfoshape==1 then
    amod oscili idepth, irate, giParabola
  elseif ilfoshape==2 then
    amod oscili idepth, irate, giSine
  elseif ilfoshape==3 then
    amod oscili idepth, irate, giTriangle
  elseif ilfoshape==4 then
    amod randomi 0, idepth, irate,1
  else
    amod randomh 0,idepth,irate,1
  endif
  adelsig flanger (aL+aR)/2, amod, ifback , 1.2
  adelsig dcblock adelsig

  xout adelsig,adelsig
endop
