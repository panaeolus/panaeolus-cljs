instr 1
  ishift = .00666667               ;shift it 8/1200.
  ipch = p5
  ioct = p5
  kadsr linseg 0, p3/3, 1.0, p3/3, 1.0, p3/3, 0 ;ADSR envelope
  kmodi linseg 0, p3/3, 5, p3/3, 3, p3/3, 0 ;ADSR envelope for I
  ip6 random 0.1, 0.4
  ip7 random 1, 3
  kmodr linseg ip6, p3, ip7              ;r moves from p6->p7 in p3 sec.
  a1 = kmodi*(kmodr-1/kmodr)/2
  a1ndx = abs(a1*2/20)            ;a1*2 is normalized from 0-1.
  a2 = kmodi*(kmodr+1/kmodr)/2
  a3 tablei a1ndx, 3, 1  ;lookup tbl in f3, normal index
  ao1 poscil a1, ipch, giCos  ;cosine
  a4 = exp(-0.5*a3+ao1)
  ao2 poscil a2*ipch, ipch, giCos        ;cosine
  aoutl poscil kadsr*a4, ao2+cpsoct(ioct+ishift)  ;fnl outleft
  ;;aoutr poscil kadsr*a4, ao2+cpsoct(ioct-ishift)  ;fnl outright
  aL = aoutl
  aR = aoutl
  
  outs aL, aR
endin
