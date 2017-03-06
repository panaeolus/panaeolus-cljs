opcode	LoFi,a,akk
  ain,kbits,kfold xin
  kvalues pow 2, kbits
  aout = (int((ain/0dbfs)*kvalues))/kvalues
  aout fold aout, kfold
  xout aout
endop
