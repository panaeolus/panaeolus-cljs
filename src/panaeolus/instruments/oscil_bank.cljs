(ns panaeolus.instruments.oscil-bank
  (:require [macchiato.fs :as fs]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [Csound csound]]
            [panaeolus.macros :refer [definstrument demo]]))

(definstrument "bass_trombone"
  (fs/slurp "src/panaeolus/csound/orchestra/oscbnk/bass_trombone.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:midi 36}
   :p6 {:fmd 0.2}})

;; (demo (group (bass_trombone :midi 92 :amp -4 :fmd 0.15))  1)
