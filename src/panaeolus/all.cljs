(ns panaeolus.all
  (:require
   panaeolus.engine
   panaeolus.algo.control
   panaeolus.instruments.tr808
   panaeolus.instruments.synths
   panaeolus.instruments.oscil-bank
   [panaeolus.macros :refer [pat-> demo seq]]))


(do
  (panaeolus.macros/pull-symbols 'panaeolus.engine)
  (panaeolus.macros/pull-symbols 'panaeolus.algo.control)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.tr808)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.oscil-bank)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.synths)
  (panaeolus.macros/pull-macros  'panaeolus.all))








