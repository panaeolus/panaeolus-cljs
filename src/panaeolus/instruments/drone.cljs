(ns panaeolus.instruments.drone
  (:require [macchiato.fs :as fs]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [csound]])
  (:require-macros [panaeolus.macros :refer [definstrument]]))

(definstrument "sruti"
  "src/panaeolus/csound/orchestra/drone/sruti.csd"
  {:p3 {:dur 1}
   :p4 {:amp -22}
   :p5 {:freq 100}
   :p6 {:num 1}
   :p7 {:den 1}
   :p8 {:risset 1}
   :p9 {:rate 0.1}})
