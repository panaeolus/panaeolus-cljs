;; From Iain McCurdy
opcode pitchshifter, a, akkkkii ; individual buffer feedback
  ainL,iratio,ifeedback,iDelay,iSmooth,imaxdelay,iwfn xin
  setksmps 1
  
  kPortTime linseg 0,0.001,1
  kratio  portk iratio, kPortTime*iSmooth 
  kDelay  portk iDelay, kPortTime*iSmooth 
  aDelay  interp kDelay
  
  arate  = (kratio-1)/kDelay  ;SUBTRACT 1/1 SPEED
  
  aphase1  phasor -arate    ;MOVING PHASE 1-0
  aphase2  phasor -arate, .5   ;MOVING PHASE 1-0 - PHASE OFFSET BY 180 DEGREES (.5 RADIANS)
  
  agate1  tablei aphase1, iwfn, 1, 0, 1  ;
  agate2  tablei aphase2, iwfn, 1, 0, 1  ;
  
  abuf1  delayr imaxdelay   ;DECLARE DELAY BUFFER
  adelsig1 deltap3 aphase1 * aDelay  ;VARIABLE TAP
  aGatedSig1 = adelsig1 * agate1
  delayw ainL + (aGatedSig1*ifeedback) ;WRITE AUDIO TO THE BEGINNING OF THE DELAY BUFFER, MIX IN FEEDBACK SIGNAL - PROPORTION DEFINED BY gkFB
  
  abuf2  delayr imaxdelay   ;DECLARE DELAY BUFFER
  adelsig2 deltap3 aphase2 * aDelay  ;VARIABLE TAP
  aGatedSig2 = adelsig2 * agate2
  delayw ainL + (aGatedSig2*ifeedback) ;WRITE AUDIO TO THE BEGINNING OF THE DELAY BUFFER, MIX IN FEEDBACK SIGNAL - PROPORTION DEFINED BY gkFB

  aGatedMixL = (aGatedSig1 + aGatedSig2) * 0.5
  xout aGatedMixL
endop

