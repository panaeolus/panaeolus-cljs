instr 1
  idur = p3
  iamp =  ampdb(p4-6)
  ibasefreq = p5
  iwave   = p6
  ires = p7
  idist = p8
  iatt = p9
  idec = p10
  irel = max:i((p3 - iatt - idec), 0.001)
  isus = p11
  ; irel = p11
  ilpf = p12
  ; iexpon = p12
  ifilt = p13
  ilpf *= ibasefreq
  ilpf limit ilpf, 0.001, sr/2
  p3 = iatt+idec+isus+irel
  iminval = 1/sr
  if ilpf < ibasefreq then
    aCfOctEnv expsegr ilpf, max:i(iminval,iatt), ibasefreq, max:i(iminval,idec), max:i(iminval,isus), ibasefreq, irel,max:i(iminval,ilpf),ilpf
    aCfOctEnv limit aCfOctEnv, ilpf, ibasefreq
  else
    aCfOctEnv expsegr ibasefreq, max:i(iminval,iatt), ilpf, max:i(iminval,isus),ilpf, max:i(iminval,idec), ibasefreq,irel,ibasefreq
    aCfOctEnv limit aCfOctEnv, ibasefreq, ilpf
  endif
  
  

  
  if iwave == 0 then
    asig poscil 0.2, ibasefreq, giSaw
  elseif iwave == 1 then
    asig poscil 0.2, ibasefreq, giSquare
  elseif iwave == 2 then
    asig poscil 0.2, ibasefreq, giTriangle
  elseif iwave == 3 then
    asig buzz 0.2, ibasefreq,  sr/4/ilpf, giSine
  elseif iwave == 4 then
    asig poscil 0.2, ibasefreq,  giDrone
  endif

  if ifilt==1 then
    ; asig zdf_ladder asig, aCfOctEnv,ires
    asig moogladder asig/2, aCfOctEnv,ires
    asig limit asig, 0, 0.8
  else
    asig lpf18 asig, aCfOctEnv ,ires, (idist^2)*20 ;LOWPASS
    ; asig *= idist
    asig butlp asig, ilpf
  endif
  ;FATTEN SOUND
  af1 resonz asig, ibasefreq,ibasefreq/3.5,1
  ; aM distort asig*5, 0.1, giSine
  aM ntrpol asig,af1*5,0.9
  aM buthp aM, 19
  ; aenv
  ; linsegr	1, 0.05, 0
  if ifilt == 1 then 
    aM *= iamp*0.3
  else
    aM *= iamp
  endif 
  aDeclick linseg 0, 0.02, 1, p3 - 0.05, 1, 0.02, 0, 0.01, 0
  aM *= aDeclick
  outs aM, aM
endin
