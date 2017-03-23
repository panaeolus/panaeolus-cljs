(ns panaeolus.instruments.synths
  (:require [macchiato.fs :as fs]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [Csound csound]] 
            [panaeolus.fx :refer [freeverb lofi flanger]])
  (:require-macros [panaeolus.macros :refer [definstrument demo forever]]))

(definstrument "nuclear"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/nuclear.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 200}
   :p6 {:lpf 500}})

(definstrument "sweet"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/sweet.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 200}})

(definstrument "asfm"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/asfm.orc")
  {:p3 {:dur 1}
   :p4 {:amp -10}
   :p5 {:freq 100}
   :p6 {:mod 3}
   :p7 {:index 1}})

(definstrument "organ"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/organ.orc")
  {:p3 {:dur 1}
   :p4 {:amp -10}
   :p5 {:freq 500}})

(definstrument "hammer"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/hammer.orc")
  {:p3 {:dur 2}
   :p4 {:amp -12}
   :p5 {:freq 200}})

(definstrument "scan"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/scan.orc")
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
  (fs/slurp "src/panaeolus/csound/orchestra/synth/tb303.orc")
  {:p3 {:dur 0.1}
   :p4 {:amp -16}
   :p5 {:freq 90}
   :p6 {:wave 4}
   :p7 {:res 1.9}
   :p8 {:dist 20}
   :p9 {:att 0.03}
   :p10 {:dec 0.1}
   :p11 {:rel 0.1}
   :p12 {:lpf 1.1} 
   :p13 {:filt 0}})

;; (demo (tb303 :freq 30 :lpf 500 :res 20 :att 0.01 :dec 0.06 :wave 1 :filt 1 :dist 200 ) 1)

;; (demo (scan :freq 300 :fx (panaeolus.fx/bp :band 100) :amp -6))

;; (demo (asfm  :freq 100 :cutoff 1200 :amp 0 :mod 2.9 :fx [(lofi :bits 4) (freeverb :sr 9000)] :index 2))

;; (demo (sweet :freq 101 :amp -20 :fx (freeverb :sr 90000)))

;; (forever (hammer :freq 60 :fx (flanger :depth 0.01 :rate 20)))

