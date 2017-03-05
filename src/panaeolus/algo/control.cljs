(ns panaeolus.algo.control
  (:require [panaeolus.engine :refer [bpm!]]))

(defn kill [env]
  (assoc env :kill true))

(defn len [env length]
  (assoc env :len length))

(defn grid
  "For sequencers, how much each beat
   is divided, defaults to 1 tick per beat."
  [env grid] (assoc env :grid (max (int grid) 1)))

(defn group
  "Group instruments togeather, in
   sequencers, the instrument identity
   becomes it's index position within
   the group."
  [& instruments]
  (vec instruments))
