instr 1
  iamp = ampdbfs(p4)
  idur = p3
  ifreq = p5
  isample = p6
  iloop = p7
  ilen ftlen isample
  isr ftsr isample
  p3 = ((ilen/isr)*(1/ifreq))
  ichannels = ftchnls(isample)
  ifreq limit ifreq, 0.00001, sr/2

  if (ichannels == 1) then
    aL loscil iamp, ifreq, isample, 1, iloop
    aR = aL
  elseif (ichannels == 2) then
    aL, aR loscil iamp, ifreq, isample, 1, iloop
  else
    aM = 0
    a1 = 0
    a2 = 0
  endif

  aenv  linseg 0, 0.0005, 1, p3 - 0.0395, 1, 0.02, 0, 0.01, 0
  aL =  aL*aenv/2
  aR =  aR*aenv/2

  outs aL, aR
endin
