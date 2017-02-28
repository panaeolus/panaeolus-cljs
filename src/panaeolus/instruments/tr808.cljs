(ns panaeolus.instruments.tr808
  (:require [macchiato.fs :as fs]
            [panaeolus.engine :refer [Csound csound]]))

(def ^:private low_conga
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/low_conga.orc"))

(def ^:private mid_conga
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/mid_conga.orc"))

(def ^:private high_conga
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/high_conga.orc"))

(def ^:private maraca
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/maraca.orc"))

(def ^:private clap
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/clap.orc"))


(do 
  (.CompileOrc csound Csound clap))

(.InputMessage csound Csound "i 1 0 2 0")
;;(.EvalCode csound Csound (fs/slurp "src/panaeolus/csound/tables.orc"))
