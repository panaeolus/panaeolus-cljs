instr 1
  idur = p3
  iamp =  ampdb(p4)
  ibasefreq = p5
  iwave   = p6
  ires = p7
  idist = p8
  iatt = p9
  idec = p10
  irel = p11
  ilpf = p12
  ; iexpon = p12
  ifilt = p13

  p3 = iatt+idec
  
  
  kCfOctEnv expsegr 0.001, iatt, ilpf, idec, ibasefreq,irel,ibasefreq
  ; kCfOctEnv limit kCfOctEnv, ilpf2, sr/2


  
  if iwave == 0 then
    asig poscil 0.2, ibasefreq, giSaw
  elseif iwave == 1 then
    asig poscil 0.2, ibasefreq, giSquare
  elseif iwave == 2 then
    asig poscil 0.2, ibasefreq, giTriangle
  elseif iwave == 3 then
    asig buzz 0.2, ibasefreq,  sr/4/ilpf, giSine
  elseif iwave == 4 then
    asig buzz 0.2, ibasefreq,  sr/4/ilpf, giDrone
  endif

  if ifilt==1 then
    asig moogladder asig, kCfOctEnv,ires		;LOWPASS
    asig limit asig, 0, 0.8
  else
    asig	lpf18	asig, kCfOctEnv ,ires, (idist^2)*20		;LOWPASS
    iSclGain2  ftgenonce  0, 0, 1024, -16, 1, 1024, -8, 0.1
    if idist > 0 then
      iGain table int(idist), iSclGain2, 1			;READ GAIN VALUE FROM RESCALING CURVE
      asig *= iGain
      asig butlp asig, ilpf
    endif
  endif
  ;FATTEN SOUND
  af1	resonz	asig,rnd(100)+30,rnd(20)*50+10,1
  aM	ntrpol	asig,af1*5,0.3
  ; aenv	linsegr	1, 0.05, 0
  aM *= iamp
  outs aM, aM
endin
