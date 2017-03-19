(ns panaeolus.algo.pitch
  (:require [panaeolus.freq :refer [midi->freq freq->midi]]))

(defn octave
  "Octave pitch shift,
   1 = Octave up
   -1 = Ocvate down"
  [env & oct]
  (let [operator (if (number? (first oct))
                   (if (neg? (first oct))
                     (/ 1 (- 1 (first oct)))
                     (+ (first oct) 1))
                   2)]
    (assoc env :freq (reduce #(if (neg? %2)
                                (conj %1 %2)
                                (conj %1 (* operator %2))) [] (:freq env)))))

(defn midi [env]
  (assoc env :freq (reduce (fn [x y] (conj x (midi->freq y))) [] (:freq env))))

