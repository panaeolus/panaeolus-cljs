(ns panaeolus.instruments.fof
  (:require [macchiato.fs :as fs]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [Csound csound]] 
            [panaeolus.fx :refer [freeverb lofi flanger]])
  (:require-macros [panaeolus.macros :refer [definstrument demo forever]]))


(definstrument "priest"
  (fs/slurp "src/panaeolus/csound/orchestra/fof/priest.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 200}
   :p6 {:morph 1}
   :p7 {:att 0.1}
   :p8 {:format 70}}  )

;; (demo (priest :freq 100) 1)
