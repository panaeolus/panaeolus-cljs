instr 1
  iTimGap = 0.01 
  idur1 = 0.02*p3
  idur2 = 2*p3
  idens = 8000
  iamp1 = 0.9*ampdb(p4)
  iamp2 = 1.5*ampdb(p4)
  if frac(p1)==0 then 
    event_i	"i", p1+0.1, 0, idur1, p4
    event_i	"i", p1+0.1, iTimGap, idur1, p4
    event_i	"i", p1+0.1, iTimGap*2, idur1, p4
    event_i	"i", p1+0.1, iTimGap*3, idur2, p4
  else
    kenv transeg	1,p3,-25,0
    iamp random	0.7,1
    anoise dust2 kenv*iamp, idens
    iBPF = 1100
    ibw = 2000
    iHPF = 1000
    iLPF = 1
    kcf	expseg 8000,0.07,1700,1,800,2,500,1,500
    asig butlp anoise,kcf*iLPF
    asig buthp asig,iHPF
    ares reson asig,iBPF,ibw,1
    asig dcblock2 (asig*iamp1)+(ares*iamp2)
    asig = asig*iamp
    aL,aR pan2 asig,0.5 - linrand:i(1)
    outs aL,aR
  endif
endin
