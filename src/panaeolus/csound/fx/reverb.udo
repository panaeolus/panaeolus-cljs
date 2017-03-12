alwayson 9000

instr 9000
  aMasterLeft zar 0
  aMasterRight zar 1
  aZakRvbL zar 2
  aZakRvbR zar 3
  kRoom chnget "roomsize" 
  aRvbL,aRvbR reverbsc aZakRvbL*0.3, aZakRvbR*0.3, kRoom, 7000 
  zawm (aRvbL+aMasterLeft), 0
  zawm (aRvbR+aMasterRight), 1
endin
