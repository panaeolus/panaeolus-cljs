opcode Exciter, aa, aaiii
  asigL,asigR,iceil,iharmonics,iblend xin
  ifreq = max:i(p5,20)
  iceilabs = min:i(sr/2, abs(iceil)*ifreq)
  iamplevel = max:i(0.1, (1 - (iharmonics/50)))
  if iceil < 0 then
    kceil line iceilabs, p3, ifreq/2
    asig exciter (asigL+asigR)*0.9, ifreq, kceil, iharmonics, iblend
  else
    adeclick linseg 0, 0.02, 1, p3 - 0.05, 1, 0.02, 0, 0.01, 0
    kceil line ifreq/2, p3, iceilabs
    asig exciter (asigL+asigR)*iamplevel, ifreq, kceil, iharmonics, iblend
    asig *= adeclick
  endif
  
  xout asig, asig
endop

;; ceil can be negative
