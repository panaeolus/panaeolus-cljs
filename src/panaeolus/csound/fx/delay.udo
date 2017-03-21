;; Iain McCurdy 2015
opcode  DelayLayer,aa,aakkkkkkkkkip
  aInSigL,aInSigR,iBase,iShape,iInterval,iScatter,iSpread,iSepMode,iHPF,iLFP,iFeedback,iLayers,iCount xin
  iRnd random  -0.5,0.5 ; i-time random value. A unique fixed value for each delay layer (and channel).
  kRnd = octave(iRnd * iScatter) ; scale random value by GUI widget control
  
  if iSepMode==1 then                                                            ; linear
    aDel interp  limit:k(kRnd/(iBase * semitone(iInterval*(iCount-1))),1/kr,2)
  else ; exponential
    aDel interp  limit:k(kRnd/(iBase * semitone((iInterval+1)^iCount)),1/kr,2)
  endif 

  iAmp = (iCount/iLayers) ^ iShape
  abuf delayr  2
  aWG deltapi aDel
  aWG atone aWG,iHPF
  aWG tone aWG,iLFP
  aWG dcblock2 aWG
  delayw (aInSigL+aInSigR) + (aWG*iFeedback)

  if iCount<iLayers then
    aMix, aMix DelayLayer aInSigL,aInSigR,iBase,iShape,iInterval,iScatter,1-iSpread,iSepMode,iHPF,iLFP,iFeedback,iLayers,iCount+1
  endif
  aOut = aWG*limit:i(iSpread*2,0,1)*iAmp + aMix
  xout aOut,aOut
  aMix = 0
  aOut = 0
endop
