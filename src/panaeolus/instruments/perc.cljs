(ns panaeolus.instruments.perc
  (:require panaeolus.macros)
  (:require-macros [panaeolus.macros :refer [definstrument demo]]))

(definstrument "kick"
  "src/panaeolus/csound/orchestra/perc/kick.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 60.3}})


;; (demo (kick -6 1990) 2)
