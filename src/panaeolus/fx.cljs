(ns panaeolus.fx
  (:import [goog.string format])
  (:require-macros [panaeolus.macros :refer [define-fx]]))

(define-fx "freq-shift"
  nil
  (fn [aL aR freq]
    (format "\n %s freqShift %s \n %s freqShift %s \n" aL freq aR freq))
  [:freq 1.2])

(define-fx "reverb"
  nil
  (fn [aL aR mix]
    (str
     (format "\n chnmix %s*%s, \"RvbL\" \n chnmix %s*%s, \"RvbR\" \n" aL mix aR mix)
     (format "%s = %s * (1-%s)\n%s = %s * (1-%s)\n" aL aL mix aR aR mix)))
  [:mix 0.8])

;;BROKEN!!
(define-fx "freeverb"
  nil
  (fn [aL aR room damp sr]
    (str (format"\n%s, %s freeverb %s, %s, %s, %s, %s\n" aL aR aL aR room damp sr)
         "kRvbEnv expsegr 1, p3, 1, p3*2, .01\n"
         aL " *= kRvbEnv\n" aR " *= kRvbEnv\n"))
  [:room 0.9 :damp 0.35 :sr 44100])

(define-fx "lofi"
  "src/panaeolus/csound/fx/lofi.udo"
  (fn [aL aR bits fold]
    (format"\n%s, %s LoFiS  %s, %s, %s, %s\n" aL aR aL aR bits fold))
  [:bits 6 :fold 0.1])

(define-fx "flanger"
  "src/panaeolus/csound/fx/flanger.udo"
  (fn [aL aR rate depth delay fback wet shape]
    (format "\n %s, %s Flanger_stereo %s, %s, %s, %s, %s, %s, %s, %s \n"
            aL aR aL aR rate depth delay fback wet shape))
  [:rate 5.15 :depth 0.001 :delay 0.001 :fback 0
   :wet 1 :shape 1])

(define-fx "delayl"
  "src/panaeolus/csound/fx/delay.udo"
  (fn [aL aR base layers ival shape
       scatter spread fback hpf
       lpf mode]
    (format  "\n%s, %s DelayLayer %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n"
             aL aR aL aR base shape ival scatter spread mode hpf lpf fback layers))
  [:base 50 :layers 8 :ival 0.07
   :shape 1 :scatter 0 :spread 0.5
   :fback 0.95 :hpf 70 :lpf 70 :mode 1])

(define-fx "perc"
  nil
  (fn [aL aR dur exp]
    (format "\np3=%f\naPercEnv expon 1,%f,%f\n%s*=aPercEnv\n%s*=aPercEnv\n"
            dur dur exp aL aR))
  [:dur 0.4 :exp 0.1])

(define-fx "butbp"
  nil
  (fn [aL aR center band]
    (format "\n%s butbp %s, %f,%f\n\n%s butbp %s, %f,%f\n"
            aL aL center band aR aR center band))
  [:center 1000 :band 100])

(define-fx "buthp"
  nil
  (fn [aL aR cutoff]
    (format "\n%s buthp %s,%f\n\n%s buthp %s, %f\n"
            aL aL cutoff aR aR cutoff))
  [:cutoff 200])

(define-fx "butlp"
  nil
  (fn [aL aR cutoff]
    (format "\n%s butlp %s,%f\n\n%s butlp %s, %f\n"
            aL aL cutoff aR aR cutoff))
  [:cutoff 1000])

(define-fx "vibrato"
  "src/panaeolus/csound/fx/vibrato.udo"
  (fn [aL aR freq delay1 delay2]
    (format "\n%s,%s Vibrato %s,%s,%s,%s,%s,giSine\n"
            aL aR aL aR freq delay1 delay2))
  [:freq 5.5 :delay1 0.1 :delay2 1.2])

(define-fx "distort"
  nil
  (fn [aL aR dist]
    (let [dist (max 0.05 dist)]
      (format (str "\n%s distort %s*exp(%f), %f,giDrone \n"
                   "\n%s distort %s*exp(%f), %f,giDrone \n")
              aL aL dist dist aR aR dist dist)))
  [:dist 0.5])

(define-fx "binauralize"
  "src/panaeolus/csound/fx/binauralize.udo"
  (fn [aL aR cent diff]
    (format "\n%s,%s binauralize %s,%s,%s\n"
            aL aR aL cent diff))
  [:cent 1.01 :diff 1])

(define-fx "pitch-shift"
  "src/panaeolus/csound/fx/pitch_shifter_2.udo"
  (fn [aL aR ratio feedback delay smooth]
    (str
     (format "\n%s pitchshifter aL,%s,%s,%s,%s,4, giTriangle2\n"
             aL ratio feedback delay smooth)
     (format "\n%s pitchshifter aL,%s,%s,%s,%s,4, giTriangle2\n"
             aR ratio feedback delay smooth)))
  [:ratio 1 :feedback 0.001 :delay 0.1 :smooth 1])

(define-fx "exciter"
  "src/panaeolus/csound/fx/exciter.udo"
  (fn [aL aR ceil harm blend]
    (format "\n%s,%s Exciter %s,%s,%s,%s,%s"
            aL aR aL aR ceil harm blend))
  [:ceil 2 :harm 12 :blend 1])

(define-fx "chorus"
  "src/panaeolus/csound/fx/chorus.udo"
  (fn [aL aR rate chaos depth offset width wet]
    (format "\n%s,%s Chorus %s,%s,%s,%s,%s,%s,%s,%s"
            aL aR aL aR rate chaos depth offset width wet))
  [:rate 5.5 :chaos 0.1 :depth 0.1 :offset 0.001 :width 0.5 :wet 1])

#_(define-fx "shred"
    "src/panaeolus/csound/fx/shred.udo"
    (fn [aL aR delay transpose rand depth
         rate fback width wet gain pre?]
      (format "\n%s,%s shred %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s"
              aL aR aL aR delay transpose rand depth
              rate fback width wet gain pre?))
    [:delay 0.1 :transpose 1.5 :rand 0.6 :depth 2
     :rate 5 :fback 0.3 :width 0.5 :wet 1 :gain 0.2 :pre? 0])

