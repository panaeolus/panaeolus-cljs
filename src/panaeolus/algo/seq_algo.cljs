(ns panaeolus.algo.seq-algo)

(defn uncycle
  "Essentally adds rests
   as filler to the pattern
   length."
  [env]
  (assoc env :uncycle? true))
