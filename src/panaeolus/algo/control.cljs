(ns panaeolus.algo.control
  (:require [panaeolus.engine :refer [bpm!]]))

(defn kill [env]
  (assoc env :kill true))

(defn len [env length]
  (assoc env :len length))

(defn meter [env meter]
  (assoc env :meter meter))

(defn grid
  "For sequencers, how much each beat
   is divided, defaults to 4 / 1 tick per beat.
   1=wholenote 2=halfnote 4=quavier 8=8th etc.."
  [env grid]
  (if (:seq-parsed? env)
    (assoc env :dur (mapv #(/ % (int (/ grid 4))) (or (:dur env) [])))
    (assoc env :grid (max (int (/ grid 4)) 1))))

(defn group
  "Group instruments togeather, in
   sequencers, the instrument identity
   becomes it's index position within
   the group."
  [& instruments]
  (vec instruments))
