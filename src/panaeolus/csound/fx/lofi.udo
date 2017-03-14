opcode	LoFi,a,akk
  ain,kbits,kfold xin
  kvalues pow 2, kbits
  aout = (int((ain/0dbfs)*kvalues))/kvalues
  aout fold aout, kfold
  xout aout
endop

opcode LoFiS,aa,aakk
  ainL,ainR,kbits,kfold xin
  kvalues pow 2, kbits
  aout = (int(((ainL+ainR)/(2*0dbfs))*kvalues))/kvalues
  aout fold aout, kfold
  xout aout,aout
endop
