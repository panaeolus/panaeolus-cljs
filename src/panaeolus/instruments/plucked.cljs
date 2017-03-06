(ns panaeolus.instruments.plucked
  (:require [macchiato.fs :as fs]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [Csound csound]]
            [panaeolus.macros :refer [definstrument demo]]))

(definstrument "pluck_bass"
  (fs/slurp "src/panaeolus/csound/orchestra/pluck/bass.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 90}
   :p6 {:dist 1}})

;; (demo (group (pluck_bass :freq 20 :amp -10 ))  10)
