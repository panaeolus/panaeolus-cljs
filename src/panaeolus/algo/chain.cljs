(ns panaeolus.algo.chain)


(chains [[1 1 1 1]
         []])

(defn chains* [v]
  (loop [v v]
    (if (empty? v)
      v
      (let []
        (recur (rest v))))))
