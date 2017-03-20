opcode  DelayLayer,a,akkkkkkkkkip
  aInSig,kBase,kShape,kInterval,kScatter,kSpread,kSepMode,kHPF,kLPF,kFeedback,iLayers,iCount xin
  iRnd random  -0.5,0.5 ; i-time random value. A unique fixed value for each delay layer (and channel).
  kRnd = octave(iRnd * kScatter) ; scale random value by GUI widget control
  
  if kSepMode==1 then                                                            ; linear
    aDel interp  limit:k(kRnd/(kBase * semitone(kInterval*(iCount-1))),1/kr,2)
  else ; exponential
    aDel interp  limit:k(kRnd/(kBase * semitone((kInterval+1)^iCount)),1/kr,2)
  endif 

  kAmp = (iCount/iLayers) ^ kShape
  abuf delayr  2
  aWG deltapi aDel
  aWG atone aWG,kHPF
  aWG tone aWG,kLPF
  aWG dcblock2 aWG
  delayw aInSig + (aWG*kFeedback)

  if iCount<iLayers then
    aMix DelayLayer aInSig,kBase,kShape,kInterval,kScatter,1-kSpread,kSepMode,kHPF,kLPF,kFeedback,iLayers,iCount+1
  endif

  xout aWG*limit:k(kSpread*2,0,1)*kAmp + aMix
  aMix = 0
  
endop
