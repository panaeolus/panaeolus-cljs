(ns panaeolus.all
  (:require
   panaeolus.algo.seq
   panaeolus.algo.seq-algo
   panaeolus.engine
   panaeolus.broker 
   panaeolus.fx
   panaeolus.algo.control
   panaeolus.orchestra-parser
   [panaeolus.orchestra-parser :as p]
   panaeolus.algo.pitch
   panaeolus.instruments.tr808
   panaeolus.instruments.fof
   panaeolus.instruments.sampler
   panaeolus.instruments.synths
   panaeolus.instruments.perc
   panaeolus.instruments.plucked
   panaeolus.instruments.oscil-bank)
  (:require-macros [panaeolus.macros :refer [ demo -> forever P]]))


(do
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
  (panaeolus.macros/pull-macros  'panaeolus.all)
  ;;(panaeolus.macros/pull-macros  'panaeolus.macros)
  )

(comment
  (pat 'melody1 (panaeolus.instruments.tr808/mid_conga)
       (-> (assoc :dur [1 1 1 0.25 0.125 0.125 0.5])
           (assoc :kill true)))

  (panaeolus.broker/pattern-loop-queue
   (do (panaeolus.instruments.tr808/low_conga)
       (-> {:dur [1 1 1 -0.25 0.25 0.5]
            :pattern-name :abc
            :input-messages "i 2 0 0.1 -8 100"
            :meter 4
            :kill true
            }
           (assoc :dur [1 1 1 0.125 0.125 0.25 0.5])))))

