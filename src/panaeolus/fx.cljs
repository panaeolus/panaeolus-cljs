(ns panaeolus.fx
  (:import [goog.string format])
  (:require-macros [panaeolus.macros :refer [define-fx]]))


(define-fx "freeverb"
  (fn [aL aR room damp sr]
    (str (format"\n%s, %s freeverb %s*1.5, %s*1.5, p%s, p%s, p%s\n" aL aR aL aR room damp sr)
         "kRvbEnv expsegr 1, p3, 1, p3*2, .01\n"
         aL " *= kRvbEnv\n" aR " *= kRvbEnv\n"))
  [:room 0.9 :damp 0.35 :sr 44100])

(define-fx "lofi"
  (fn [aL aR bits fold]
    (format"\n%s, %s LoFiS  %s, %s, %s, %s\n" aL aR aL aR bits fold))
  [:bits 6 :fold 0.1])

(define-fx "flanger"
  (fn [aL aR rate depth delay fback wet shape]
    (format "\n %s, %s Flanger_stereo %s, %s, %s, %s, %s, %s, %s, %s \n"
            aL aR aL aR rate depth delay fback wet shape))
  [:rate 5.15 :depth 0.001 :delay 0.001 :fback 0
   :wet 1 :shape 1])

(define-fx "delayl"
  (fn [aL aR base layers ival shape
       scatter spread fback hpf
       lpf mode]
    (format  "\n%s, %s DelayLayer %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n"
             aL aR aL aR base shape ival scatter spread mode hpf lpf fback layers))
  [:base 50 :layers 8 :ival 0.07
   :shape 1 :scatter 0 :spread 0.5
   :fback 0.95 :hpf 70 :lpf 70 :mode 1])

(define-fx "perc"
  (fn [aL aR dur exp]
    (format "\np3=%f\naPercEnv expon 1,%f,%f\n%s*=aPercEnv\n%s*=aPercEnv\n"
            (* 1.1 dur) dur exp aL aR))
  [:dur 0.4 :exp 0.1])

(define-fx "butbp"
  (fn [aL aR center band]
    (format "\n%s butbp %s, %f,%f\n\n%s butbp %s, %f,%f\n"
            aL aL center band aR aR center band))
  [:center 1000 :band 100])

(define-fx "buthp"
  (fn [aL aR cutoff]
    (format "\n%s buthp %s,%f\n\n%s buthp %s, %f\n"
            aL aL cutoff aR aR cutoff))
  [:cutoff 200])

(define-fx "butlp"
  (fn [aL aR cutoff]
    (format "\n%s butlp %s,%f\n\n%s butlp %s, %f\n"
            aL aL cutoff aR aR cutoff))
  [:cutoff 1000])

(define-fx "vibrato"
  (fn [aL aR freq delay1 delay2]
    (format "\n%s,%s Vibrato %s,%s,%f,%f,%f,giSine\n"
            aL aR aL aR freq delay1 delay2))
  [:freq 5.5 :delay1 0.1 :delay2 1.2])

(define-fx "distort"
  (fn [aL aR dist]
    (let [dist (max 0.05 dist)]
      (format (str "\n%s distort %s*exp(%f), %f,giDrone \n"
                   "\n%s distort %s*exp(%f), %f,giDrone \n")
              aL aL dist dist aR aR dist dist)))
  [:dist 0.5])

