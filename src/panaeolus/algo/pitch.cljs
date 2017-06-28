(ns panaeolus.algo.pitch
  (:require [panaeolus.freq :refer [midi->freq freq->midi
                                    scale-from-midi]]))

;; URGENT CHECK [:freq] vs :freq

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
                                (conj %1 (* operator %2))) [] (or (get env :freq)
                                                                  (get env [:freq]))))))

(defn midi [env]
  (letfn [(toMidi [freq]
            (reduce (fn [x y] (conj x (midi->freq y))) [] freq))]
    (assoc env :freq (let [freq (or (get env :freq)
                                    (get env [:freq]))]
                       (if (or (not (seqable? freq))
                               (not (seqable? (first freq))))
                         (toMidi freq)
                         (mapv toMidi freq))))))

(defn scale [env root & mode]
  (let [freq (:freq env)
        mode (if-not (empty? mode)
               (first mode) :pentatonic)]
    (if (seqable? root)
      (assoc env :freq
             (reduce (fn [init r]
                       (conj init (mapv (fn [f] (nth (scale-from-midi r mode :span (max (count freq) (apply max freq))) f)) freq))) [] root))
      (assoc env :freq (mapv #(nth (scale-from-midi root mode :span (max (count freq) (apply max freq))) %) freq)))))


;; (scale {:dur [1 1 1 1 1 1 1], :seq-parsed? true, :xtralen 0, :freq [0 0 1 3 2 0 7], :len 16, :kill true} :major)
;; (scale {:freq [32 34 36 ]} [:c3 :d3] )
