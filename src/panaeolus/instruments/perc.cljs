(ns panaeolus.instruments.perc
  (:require [macchiato.fs :as fs]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [csound]])
  (:require-macros [panaeolus.macros :refer [definstrument demo forever]]))

(definstrument "kick"
  "src/panaeolus/csound/orchestra/perc/kick.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 60.3}})


;; (demo (kick :freq 80 :amp -1) 1)
