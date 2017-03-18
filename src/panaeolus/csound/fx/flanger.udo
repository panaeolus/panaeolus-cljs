opcode Flanger_stereo,aa,aakkkkkk ;MADE BY IAIN MCCURDY
  aL,aR,krate,kdepth,kdelay,kfback,kmix, klfoshape xin
  adlt interp kdelay
  if klfoshape==1 then
    amod oscili kdepth, krate, giParabola ;oscillator that makes use of the positive domain only u-shape parabola
  elseif klfoshape==2 then
    amod oscili kdepth, krate, giSine ;oscillator that makes use of the positive domain only sine wave
  elseif klfoshape==3 then
    amod oscili kdepth, krate, giTriangle ;oscillator that makes use of the positive domain only triangle
  elseif klfoshape==4 then
    amod randomi 0,kdepth,krate,1
  else
    amod randomh 0,kdepth,krate,1
  endif
  adlt sum adlt, amod ;static delay time and modulating delay time are summed
  adelsigL flanger aL, adlt, kfback , 1.2 ;flanger signal created
  adelsigL dcblock adelsigL
  adelsigR flanger aR, adlt, kfback , 1.2 ;flanger signal created
  adelsigR dcblock adelsigR

  aL sum aL*(1-kmix), adelsigL*kmix ;create dry/wet mix
  aR sum aR*(1-kmix), adelsigR*kmix
  xout aL,aR ;send audio back to caller instrument
endop
