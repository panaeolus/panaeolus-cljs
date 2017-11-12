opcode binauralize, aa, akk
  ;; setksmps 1
  ain,kcent,kdiff	xin
  ifftsz = 2048
  ; determine pitches
  kp1		= kcent + (kdiff/2)
  kp2		= kcent - (kdiff/2)
  krat1	= kp1 / max:k(kcent, 1)
  krat2	= kp2 / max:k(kcent, 1)
  ; take it apart
  fsig pvsanal	ain, ifftsz, ifftsz/4, ifftsz, 1
  ; create derived streams
  fbinL	pvscale	fsig, krat1, 1
  fbinR	pvscale	fsig, krat2, 1
  ; put it back together
  abinL	pvsynth	fbinL
  abinR	pvsynth	fbinR
  ; send it out
  xout	abinL, abinR
  ;; clear abinL, abinR, ain
endop
