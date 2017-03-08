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


