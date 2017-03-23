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


