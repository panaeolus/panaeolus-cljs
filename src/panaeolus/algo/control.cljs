(ns panaeolus.algo.control
  (:require [panaeolus.engine :refer [bpm!]]))

(defn kill [env]
  (assoc env :kill true))

(defn len [env length]
  (assoc env :len length))
