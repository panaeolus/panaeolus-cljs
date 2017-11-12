(ns panaeolus.instruments.plucked
  (:require [macchiato.fs :as fs]
            [panaeolus.engine :refer [csound]])
  (:require-macros [panaeolus.macros :refer [definstrument demo forever]]))

(definstrument "pluck"
  "src/panaeolus/csound/orchestra/pluck/pluck.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 90}
   :p6 {:dist 1}})

(definstrument "fmpluck5"
  "src/panaeolus/csound/orchestra/pluck/fmpluck.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 90}})


;; (demo (fmpluck5 :freq 150 :amp -2 :dur 0.5))


