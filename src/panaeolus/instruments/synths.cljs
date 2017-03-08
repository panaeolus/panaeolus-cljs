(ns panaeolus.instruments.synths
  (:require [macchiato.fs :as fs]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [Csound csound]]
            [panaeolus.macros :refer [definstrument demo]]))

(definstrument "nuclear"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/nuclear.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 40}
   :p6 {:cutoff 500}})

(definstrument "sweet"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/sweet.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 200}})

(definstrument "asfm"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/asfm.orc")
  {:p3 {:dur 1}
   :p4 {:amp -10}
   :p5 {:freq 200}
   :p6 {:mod 5.5}
   :p7 {:index 1}})

(definstrument "organ"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/organ.orc")
  {:p3 {:dur 1}
   :p4 {:amp -10}
   :p5 {:freq 100}})


(demo (organ :freq 2201 :amp -20) 2)

