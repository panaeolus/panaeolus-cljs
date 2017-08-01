(ns panaeolus.all
  (:require panaeolus.algo.nseq
            panaeolus.algo.seq-algo
            panaeolus.engine
            panaeolus.broker
            panaeolus.fx
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
            panaeolus.instruments.oscil-bank)
  (:require-macros
   [panaeolus.macros :refer [pull-macros demo -> forever
                             definstrument define-fx
                             pull-symbols]]))

(def panaeolus-all-requires
  (keys (get-in @cljs.env/*compiler*
                [:cljs.analyzer/namespaces
                 'panaeolus.all
                 :requires])))

(defn pull-panaeolus [into-namespace]
  (run! #(panaeolus.macros/pull-symbols % into-namespace)
        panaeolus-all-requires)
  (panaeolus.macros/pull-macros 'panaeolus.all into-namespace))


(doseq [namespace (keys (:cljs.analyzer/namespaces @cljs.env/*compiler*))]
  (let [require-keys (keys (get-in @cljs.env/*compiler*
                                   [:cljs.analyzer/namespaces
                                    namespace
                                    :requires]))
        filterd (filter #(= 'panaeolus.all %) require-keys)]
    (when-not (empty? filterd)
      (panaeolus.all/pull-panaeolus namespace))))


