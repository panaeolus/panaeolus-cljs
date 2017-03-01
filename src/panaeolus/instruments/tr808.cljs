(ns panaeolus.instruments.tr808
  (:require [macchiato.fs :as fs]
            [panaeolus.engine :refer [Csound csound]]
            [panaeolus.orchestra-parser :refer [compile-csound-instrument]]))

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

(compile-csound-instrument "lowConga" low_conga)

(compile-csound-instrument "midConga" mid_conga)

(compile-csound-instrument "highConga" high_conga)

(compile-csound-instrument "maraca" maraca)

(compile-csound-instrument "clap" clap)


(do
  (.EvalCode csound Csound low_conga)
  (.CompileOrc csound Csound mid_conga)
  (.CompileOrc csound Csound high_conga)
  (.CompileOrc csound Csound maraca)
  (.CompileOrc csound Csound clap))

(.InputMessage csound Csound "i 199 0 0.2 0")
;;(.EvalCode csound Csound (fs/slurp "src/panaeolus/csound/tables.orc"))
