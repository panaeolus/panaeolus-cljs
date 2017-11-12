opcode shred,aa,aaiiiiiiiiii
  aL,aR,\
  iMaxDelay,iTransPose,iTransRand,\
  iDepth,iRate,iFeedback,iWidth,iwet,\
  iGain,iPrePost xin
  ;; iFFTsizes[] fillarray 128,256,512,1024,2048,4096 ;arrayofFFTsizevalues
  iFFTsize = 4*ksmps

  fsigInL pvsanal aL,iFFTsize,iFFTsize/4,iFFTsize,1 ;FFTanalyseaudio
  fsigInR pvsanal aR,iFFTsize,iFFTsize/4,iFFTsize,1 ;FFTanalyseaudio
  fsigFB pvsinit iFFTsize ;initialisefeedbacksignal
  fsigMixL pvsmix fsigInL,fsigFB ;mixfeedbackwithinput
  fsigMixR pvsmix fsigInR,fsigFB ;mixfeedbackwithinput

  iHandle1,kTime pvsbuffer fsigMixL,i(iMaxDelay) ;createacircularfsigbuffer
  kDly1 randomh 0,iMaxDelay*iDepth,iRate,1 ;delaytime
  iTranspose1_2 random iTransPose-(2*iTransPose*iTransRand),iTransPose
  fsigOut pvsbufread kTime-kDly1,iHandle1 ;readfrombuffer
  fsigGran pvsgain fsigOut,1-iGain
  fScale pvscale fsigGran,semitone(iTranspose1_2)
  fsigFB pvsgain fScale,iFeedback ;createfeedbacksignalfornextpass
  if iPrePost == 1 then
    aDly pvsynth fsigGran ;resynthesisereadbufferoutput
  else
    aDly pvsynth fScale ;resynthesisereadbufferoutput
  endif
  aMix1 ntrpol aL,aDly,iwet ;dry/wetaudiomix

  iHandle2,kTime pvsbuffer fsigMixR,iMaxDelay ;createacircularfsigbuffer
  kDly2 randomh 0,iMaxDelay*iDepth,iRate,1 ;delaytime
  iTranspose2_2 random iTransPose-(2*iTransPose*iTransRand),iTransPose
  fsigOut pvsbufread kTime-kDly2,iHandle2 ;readfrombuffer
  fsigGran pvsgain fsigOut,1-iGain
  fScale pvscale fsigGran,semitone(iTranspose2_2)
  fsigFB pvsgain fScale,iFeedback ;createfeedbacksignalfornextpass
  if iPrePost == 1 then
    aDly pvsynth fsigGran ;resynthesisereadbufferoutput
  else
    aDly pvsynth fScale ;resynthesisereadbufferoutput
  endif
  aMix2 ntrpol aR,aDly,iwet ;dry/wetaudiomix
  xout aMix2+aMix1*(1-iWidth),aMix2*(1-iWidth)+aMix1
endop
