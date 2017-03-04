(ns panaeolus.all
  (:require
   panaeolus.algo.control
   panaeolus.instruments.tr808
   [panaeolus.macros :refer [pat-> demo]]))

(do 
  (panaeolus.macros/pull-symbols 'panaeolus.all-require-macros)
  (panaeolus.macros/pull-symbols 'panaeolus.algo.control)
  (panaeolus.macros/pull-symbols 'panaeolus.instruments.tr808)
  (panaeolus.macros/pull-macros  'panaeolus.all))







