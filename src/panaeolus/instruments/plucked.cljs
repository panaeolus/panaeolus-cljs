(ns panaeolus.instruments.plucked
  (:require [macchiato.fs :as fs]
            [panaeolus.fx :refer [delayl freeverb flanger]]
            [panaeolus.engine :refer [csound]])
  (:require-macros [panaeolus.macros :refer [definstrument demo forever]]))

(definstrument "pluck"
  "src/panaeolus/csound/orchestra/pluck/pluck.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 90}
   :p6 {:dist 1}})


;; (demo (pluck :freq 1190 :amp -2 ))


