(ns panaeolus.utils)

(defn process-arguments
  "Process the given arguments so that
   a function can be given (f 1 2 3 :somekey 4)
   where all values besides keywords are processed
   sequenceially. Keyword represent a parameter name."
  [param-vector arg-env]
  (let [default-arg-map (apply hash-map param-vector)]
    (loop [default-params (partition 2 param-vector)
           given-params arg-env
           param-map {}]
      (if (empty? given-params)
        (merge default-arg-map param-map)
        (recur (rest default-params)
               (if (keyword? (first given-params))
                 (rest (rest given-params))
                 (rest given-params))
               (if (and (keyword? (first given-params))
                        (<= 2 (count given-params)))
                 (assoc param-map (first given-params) (second given-params))
                 (assoc param-map (ffirst default-params) (first given-params))))))))
