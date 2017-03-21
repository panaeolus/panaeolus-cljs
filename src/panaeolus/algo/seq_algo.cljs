(ns panaeolus.algo.seq-algo)

(defn uncycle
  "Essentally adds rests
   as filler to the pattern
   length."
  [env]
  (assoc env :uncycle? true))


(defn shuffle
  "If given seqable element
   then it behaves like clj.core/shuffle"
  [env]
  (if (seqable? env)
    (cljs.core/shuffle env)
    (let [durs (:dur env)
          freq (:freq env)]
      (prn durs freq)
      (assoc env :dur durs))))
