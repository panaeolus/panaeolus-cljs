(ns panaeolus.instruments.synths
  (:require [macchiato.fs :as fs]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [csound-compile-file]])
  (:require-macros [panaeolus.macros :refer [definstrument demo forever]]))

(definstrument "pulser"
  "src/panaeolus/csound/orchestra/synth/pulser.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 200}
   :p6 {:env 1}})

(definstrument "pm"
  "src/panaeolus/csound/orchestra/synth/pm.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 200}
   :p6 {:index1 0.3}
   :p7 {:index2 0.2}
   :p8 {:index3 0.1}
   :p9 {:ratio1 1.1}
   :p10 {:ratio2 1.7}
   :p11 {:ratio3 2.05}
   :p12 {:car 0.6}})

(definstrument "nuclear"
  "src/panaeolus/csound/orchestra/synth/nuclear.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 200}
   :p6 {:lpf 500}})

(definstrument "sweet"
  "src/panaeolus/csound/orchestra/synth/sweet.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 200}})

(definstrument "asfm"
  "src/panaeolus/csound/orchestra/synth/asfm.orc"
  {:p3 {:dur 1}
   :p4 {:amp -6}
   :p5 {:freq 100}
   :p6 {:mod 0.1}
   :p7 {:index 0.1}})

(csound-compile-file "src/panaeolus/csound/udo/Partial.udo")

(definstrument "organ"
  "src/panaeolus/csound/orchestra/synth/organ.orc"
  {:p3 {:dur 1}
   :p4 {:amp -10}
   :p5 {:freq 500}})

(definstrument "hammer"
  "src/panaeolus/csound/orchestra/synth/hammer.orc"
  {:p3 {:dur 2}
   :p4 {:amp -12}
   :p5 {:freq 200}})

(definstrument "scan"
  "src/panaeolus/csound/orchestra/synth/scan.orc"
  {:p3 {:dur 2.5}
   :p4 {:amp -9}
   :p5 {:freq 140}
   :p6 {:rate 0.01}
   :p7 {:lpf 1200}
   :p8 {:res 0.4}
   :p9 {:lfo1 0}
   :p10 {:lfo2 0}
   :p11 {:lfo3 0}
   :p12 {:lfo4 0}
   :p13 {:type 1}
   :p14 {:mass 3}
   :p15 {:stif 0.01}
   :p16 {:center 0.1}
   :p17 {:damp -0.005}
   :p18 {:pos 0}
   :p19 {:y 0}}  )

(definstrument "tb303"
  "src/panaeolus/csound/orchestra/synth/tb303.orc"
  {:p3 {:dur 0.1}
   :p4 {:amp -16}
   :p5 {:freq 90}
   :p6 {:wave 4}
   :p7 {:res 1.9}
   :p8 {:dist 20}
   :p9 {:att 0.03}
   :p10 {:dec 0.1}
   :p11 {:sus 0}
   :p12 {:lpf 1.1} 
   :p13 {:filt 0}})

(definstrument "wobbly"
  "src/panaeolus/csound/orchestra/synth/wobbly.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 100}
   :p6 {:div 2}
   :p7 {:res 0.3}})

;; (demo (tb303 :freq 40 :lpf 1.4 :res 20 :att 0.01 :dec 0.1 :sus 0.01 :wave 4 :filt 1 :dist 2  :amp -1 0.5) 0.9)

;; (demo (scan :freq 300 :fx (panaeolus.fx/bp :band 100) :amp -6))

;; (demo (asfm  :freq 100 :cutoff 1200 :amp 0 :mod 2.9 :fx [(lofi :bits 4) (freeverb :sr 9000)] :index 2))

;; (demo (wobbly :div 2 :amp -12) 1)

;; (forever (hammer :freq 60 :fx (flanger :depth 0.01 :rate 20)))

