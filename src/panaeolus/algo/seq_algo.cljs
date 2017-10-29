(ns panaeolus.algo.seq-algo)

(defn uncycle
  "Essentally adds rests
   as filler to the pattern
   length."
  [env]
  (assoc env :uncycle? true))


(defn scramble
  [env]
  (let [durs (shuffle (:dur env))
        freq (shuffle (:freq env))] 
    (assoc env :dur durs :freq freq)))


(defn wrand 
  "given a vector of slice sizes, returns the index of a slice given a
  random spin of a roulette wheel with compartments proportional to
  slices."
  [slices]
  (let [total (reduce + slices)
        r (rand total)]
    (loop [i 0 sum 0]
      (if (< r (+ (slices i) sum))
        i
        (recur (inc i) (+ (slices i) sum))))))

(defn dotted
  "Takes even lengthed durs and breaks the rythm
   into a dotted one of long-short."
  [env]
  (let [dur (:dur env)
        new-dur (vec (map-indexed
                      (fn [index dur]
                        (if (even? index)
                          (* dur 1.5)
                          (* dur 0.5)))
                      dur))]
    (assoc env :dur new-dur)))



(defn one-two
  "Takes even lengthed durs and breaks the rythm
   into a [1 0.5 0.5] rythm."
  [env]
  (let [dur (:dur env)
        new-dur (vec (map-indexed
                      (fn [index dur]
                        (let [index (mod index 3)]
                          (if (zero? index)
                            dur
                            (* dur 0.5))))
                      dur))]
    (assoc env :dur new-dur)))


(defn two-one
  "Takes even lengthed durs and breaks the rythm
   into a [0.5 0.5 1] rythm."
  [env]
  (let [dur (:dur env)
        new-dur (vec (map-indexed
                      (fn [index dur]
                        (let [index (mod index 3)]
                          (if (or (zero? index)
                                  (= 1 index))
                            (* dur 0.5)
                            dur)))
                      dur))]
    (assoc env :dur new-dur)))
