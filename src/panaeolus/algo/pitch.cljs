(ns panaeolus.algo.pitch
  (:require [panaeolus.freq :refer [midi->freq freq->midi
                                    scale-from-midi]]))

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

(defn scale [env mode & position]
  (let [freq (:freq env)
        position (if-not (empty? position)
                   (first position) 0)]
    (assoc env :freq (mapv #(nth (scale-from-midi position mode :span (max (count freq) (apply max freq))) %) freq))))


;; (scale {:dur [1 1 1 1 1 1 1], :seq-parsed? true, :xtralen 0, :freq [0 0 1 3 2 0 7], :len 16, :kill true} :major)
;; (scale {:freq [32 34 36 ]} :major )
