(ns panaeolus.all)

(require
 'panaeolus.algo.seq
 'panaeolus.algo.seq-algo
 'panaeolus.engine
 '[panaeolus.broker :refer [P]]
 'panaeolus.fx
 'panaeolus.algo.control
 'panaeolus.orchestra-parser
 '[panaeolus.orchestra-parser :as p]
 'panaeolus.algo.pitch
 'panaeolus.instruments.tr808
 'panaeolus.instruments.fof
 'panaeolus.instruments.drone
 'panaeolus.instruments.sampler
 'panaeolus.instruments.synths
 'panaeolus.instruments.perc
 'panaeolus.instruments.plucked
 'panaeolus.instruments.oscil-bank)

;; VERY HACKY
(swap! cljs.env/*compiler* assoc-in
       [:cljs.analyzer/namespaces
        'panaeolus.all
        :use-macros]
       '{demo panaeolus.macros,
         -> panaeolus.macros,
         forever panaeolus.macros,
         pull-macros panaeolus.macros})

(require-macros '[panaeolus.macros :refer [pull-macros demo -> forever]])

(defn pull-panaeolus []
  (panaeolus.macros/pull-symbols 'panaeolus.broker)
  (panaeolus.macros/pull-symbols 'panaeolus.engine)
  (panaeolus.macros/pull-symbols 'panaeolus.fx)
  (panaeolus.macros/pull-symbols 'panaeolus.algo.control)
  (panaeolus.macros/pull-symbols 'panaeolus.algo.pitch)
  (panaeolus.macros/pull-symbols 'panaeolus.algo.seq)
  (panaeolus.macros/pull-symbols 'panaeolus.algo.seq-algo)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.tr808)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.oscil-bank)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.fof)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.sampler)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.synths)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.perc) 
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.plucked)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.drone) 
  (panaeolus.macros/pull-macros  'panaeolus.all))

(pull-panaeolus)

