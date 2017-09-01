
;; (do (js/require "./lib/libcsound.js") nil)

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
            panaeolus.instruments.oscil-bank
            [cljs.core.async :as async
             :refer [<! >! chan timeout take! put!]]
            ;;libcsound
            )
  (:require-macros
   [panaeolus.macros :refer [pull-macros demo forever
                             definstrument define-fx
                             pull-symbols]]
   [cljs.core.async.macros :refer [go go-loop]]))

;; (def panaeolus-all-loaded-channel (chan 1))

#_(go (>! panaeolus-all-loaded-channel
          (keys (get-in @cljs.env/*compiler*
                        [:cljs.analyzer/namespaces
                         'panaeolus.all
                         :requires]))))

(def panaeolus-all-requires
  '[panaeolus.algo.nseq
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
    panaeolus.instruments.oscil-bank])

(defn pull-panaeolus [into-namespace panaeolus-all-requires]
  (run! #(panaeolus.macros/pull-symbols % into-namespace)
        panaeolus-all-requires)
  (panaeolus.macros/pull-macros 'panaeolus.all into-namespace))


(let [;;panaeolus-all-requires (<! panaeolus-all-loaded-channel)
      ]
  (doseq [namespace (keys (:cljs.analyzer/namespaces @cljs.env/*compiler*))]
    (let [require-keys (keys (get-in @cljs.env/*compiler*
                                     [:cljs.analyzer/namespaces
                                      namespace
                                      :requires]))
          filterd (filter #(= 'panaeolus.all %) require-keys)]
      (when-not (empty? filterd)
        (panaeolus.all/pull-panaeolus namespace panaeolus-all-requires)))))

