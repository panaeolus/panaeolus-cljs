(ns panaeolus.all
  (:require panaeolus.algo.nseq
            panaeolus.algo.seq-algo
            panaeolus.engine
            panaeolus.broker
            panaeolus.fx
            panaeolus.samples
            panaeolus.algo.control
            panaeolus.orchestra-parser
            panaeolus.algo.pitch
            panaeolus.instruments.tr808
            panaeolus.instruments.fof
            panaeolus.instruments.drone
            panaeolus.instruments.sampler
            panaeolus.instruments.synths
            panaeolus.instruments.perc
            panaeolus.instruments.plucked
            panaeolus.instruments.oscil-bank
            [cljs.core.async :as async
             :refer [<! >! chan timeout take! put!]]
            ;;libcsound
            )
  (:require-macros
   [panaeolus.macros :refer [pull-macros demo forever
                             definstrument
                             pull-symbols pat]]
   [cljs.core.async.macros :refer [go go-loop]]))


(def panaeolus-all-requires
  '[panaeolus.algo.nseq
    panaeolus.algo.seq-algo
    panaeolus.engine
    panaeolus.broker
    panaeolus.fx
    panaeolus.samples
    panaeolus.algo.control
    panaeolus.orchestra-parser
    panaeolus.algo.pitch
    panaeolus.instruments.tr808
    panaeolus.instruments.fof
    panaeolus.instruments.drone
    panaeolus.instruments.sampler
    panaeolus.instruments.synths
    panaeolus.instruments.perc
    panaeolus.instruments.plucked
    panaeolus.instruments.oscil-bank])

(def panaeolus-all-macros
  '{demo panaeolus.macros,
    define-fx panaeolus.macros,
    definstrument panaeolus.macros,
    forever panaeolus.macros
    pat panaeolus.macros})

(defn pull-panaeolus [into-namespace panaeolus-all-requires]
  (run! #(panaeolus.macros/pull-symbols % into-namespace)
        panaeolus-all-requires)
  (panaeolus.macros/pull-macros panaeolus-all-macros into-namespace))


(doseq [namespace (keys (:cljs.analyzer/namespaces @cljs.env/*compiler*))]
  (let [require-keys (keys (get-in @cljs.env/*compiler*
                                   [:cljs.analyzer/namespaces
                                    namespace
                                    :requires]))
        filterd (filter #(= 'panaeolus.all %) require-keys)]
    (when-not (empty? filterd)
      (panaeolus.all/pull-panaeolus namespace panaeolus-all-requires))))


