(ns panaeolus.instruments.tr808
  (:require [macchiato.fs :as fs]
            [panaeolus.engine :refer [Csound csound]]
            [panaeolus.macros :refer [definstrument demo]]))

(definstrument "low_conga"
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/low_conga.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}})

(definstrument "mid_conga"
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/mid_conga.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}})

(definstrument "high_conga"
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/high_conga.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}})

(definstrument "maraca"
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/maraca.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}})

(definstrument "clap"
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/clap.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}})

(comment 
  (demo (low_conga :amp 0))
  (demo (mid_conga :amp 0))
  (demo (high_conga :amp 0))
  (demo (maraca :amp 0))
  (demo (clap :amp 0))
  )


