instr 1
  iamp = ampdbfs(p4)
  idur = p3
  ifreq = p5
  isample = p6
  isamplefreq = p7
  ilen ftlen isample
  isr ftsr isample
  p3 = ilen/isr
  ichannels = ftchnls(isample)

  if (ichannels == 1) then
    ; idel = 2/ifreq
    ; kdel port idel,0.01,0.1
    aL loscil iamp, 1, isample, 1, 0
    ; pitchshifter
    aL pitchshifter aL,ifreq/isamplefreq,0.001,0.1,1,4, giTriangle2
    aR = aL
  elseif (ichannels == 2) then
    aL, a0 loscil iamp, 1, isample, 1, 0
    aL pitchshifter aL,ifreq/isamplefreq,0.001,0.1,1,4, giTriangle2
    aR = aL
    ; aL PitchShifter aL, ifreq/isamplefreq, 0.01,giSaw
    ; aR PitchShifter aR, ifreq/isamplefreq, 0.01,giSaw
  else
    aL = 0
    aR = 0
  endif 
  
  aenv  linseg 0, 0.0005, 1, p3 - 0.0395, 1, 0.02, 0, 0.01, 0
  aL =  aL*aenv/2
  aR =  aR*aenv/2

  outs aL, aR
endin
