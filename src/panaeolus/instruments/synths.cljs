(ns panaeolus.instruments.synths
  (:require [macchiato.fs :as fs]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [Csound csound]]
            [panaeolus.macros :refer [definstrument demo]]
            [panaeolus.fx :refer [freeverb lofi]]))

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
   :p5 {:freq 100}
   :p6 {:mod 3}
   :p7 {:index 1}})

(definstrument "organ"
  (fs/slurp "src/panaeolus/csound/orchestra/synth/organ.orc")
  {:p3 {:dur 1}
   :p4 {:amp -10}
   :p5 {:freq 500}})


(demo (asfm  :freq 100 :cutoff 1200 :amp 0 :mod 2.9 :fx [(lofi :bits 4) (freeverb :sr 9000)] :index 2))

(demo (sweet :freq 101 :amp -20 :fx (freeverb :sr 90000)))

