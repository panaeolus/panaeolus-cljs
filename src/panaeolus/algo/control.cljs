(ns panaeolus.algo.control)

(defn kill [env]
  (assoc env :kill true))
