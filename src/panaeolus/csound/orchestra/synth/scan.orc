instr 1
  iamp = ampdb(p4)
  irate = p6
  ilpf	= p7
  ireso = p8
  iatt	= 0.1
  isus	= 0.8
  irel	= 0.2
  ilfoamp	= p9
  ilfoamp limit ilfoamp, 0, 60
  ilforate = p10
  ilfoamp2 = p11	;lfo x filter
  ilforate2 = p12
  itype = p13
  klfo	lfo ilfoamp,ilforate,2
  kfreq = p5+klfo ;controllo midi

  ifnvel	=	giSCANvel
  ifnmass	=	giSCANmass
  if itype == 1 then
    ifnstif = giSCANspring1
  elseif itype == 2 then
    ifnstif = giSCANspring2
  elseif itype == 3 then
    ifnstif = giSCANspring3
  elseif itype == 4 then
    ifnstif = giSCANspring4
  else
    ifnstif	= giSCANspring
  endif
  ifnctr	=	giSCANcenter
  ifndamp	=	giSCANdamp
  imass	= p14
  istif	= p15
  icentr = p16
  idamp	= p17
  ileft	= .5
  iright = .5
  ipos = p18
  iy = p19/200
  ain = 0
  idisp	=	0
  id	=	0
  scanu	giSCANinit,.01+irate,ifnvel,ifnmass,ifnstif,ifnctr,ifndamp,imass,istif,icentr,idamp,ileft,iright,ipos,iy,ain,idisp,id
  asig	scans	iamp/20,kfreq,giSCANtra1,id, 1
  ;filter
  klfofilter lfo ilfoamp2,ilforate2,3
  amoog	moogladder asig,ilpf+klfofilter,ireso
  aout	balance	amoog,asig
  ;master
  acL	clip	aout,0,.8
  if p3 < 0 then
    kenv linsegr 1, .05, 0.5, 5, 0
    acL = acL * kenv
  else
    acL linenr acL,0.01,p3,0.1
  endif 
  outs	acL, acL
endin
