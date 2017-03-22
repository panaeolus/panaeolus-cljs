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
  iminval = 1/sr
  aCfOctEnv expsegr 0.001, max:i(iminval,iatt), ilpf, max:i(iminval,idec), ibasefreq,irel,max:i(iminval,ibasefreq)
  ; aCfOctEnv limit aCfOctEnv, ibasefreq, ilpf

  
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
    asig moogladder asig/2, aCfOctEnv,ires
    asig limit asig, 0, 0.8
  else
    asig lpf18 asig, aCfOctEnv ,ires, (idist^2)*20 ;LOWPASS
    ; asig *= idist
    asig butlp asig, ilpf
  endif
  ;FATTEN SOUND
  af1	resonz	asig,rnd(100)+30,rnd(20)*50+10,1
  aM	ntrpol	asig,af1*5,0.3
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
