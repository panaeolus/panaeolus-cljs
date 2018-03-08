(ns panaeolus.score-parser)


(defn generate-p-keywords [p-count]
  (map #(keyword (str "p" %)) (range 3 (+ p-count 3))))

;; (generate-p-keywords 6)
;; Used by definst in macros
(defn fold-hashmap [h-map]
  (reduce-kv #(assoc %1 %2 (if (vector? %3)
                             %3 [(first (keys %3))])) {} h-map))

;; Todo, create test for this
(defn fill-the-bar [v meter]
  (let [cnt-v (count v)]
    (loop [filled-v []
           sum 0
           indx 0]
      (let [next (nth v (mod indx cnt-v))]
        (if (< meter (+ (Math/abs next) sum))
          filled-v
          (recur (conj filled-v next)
                 (+ (Math/abs next) sum)
                 (inc indx)))))))

(defn determine-meta-recurcion [env param-list instr]
  (loop [params param-list
         recurcion-level 0]
    (if (empty? params)
      recurcion-level
      (let [param ((first params) (nth instr 1))
            val (or (get-in env param) (get-in (nth instr 2) param)
                    (get env param) (get (nth instr 2) param)
                    (do (prn "Warning, bad default in determine-meta-recurcion") 0))]
        (recur (rest params)
               (if-not (seqable? val)
                 recurcion-level
                 (if (seqable? (first val))
                   (max (count val) recurcion-level)
                   recurcion-level)))))))

;; Note to self
;; here the dur is really p3 but not
;; the duration between events.

#_(defn ast-input-messages-builder [env instr]
    (let [dur' (if-let [d (:dur env)]
                 (if (vector? d) d [d])
                 ;; Should not be possible to reach this case.
                 [0.25]) 
          dur (remove #(or (zero? %) (neg? %)) dur')
          len (if (:uncycle? env)
                (count dur)
                (if-let [len (:len env)]
                  (+ (->> (vec (take (:len env) (cycle dur')))
                          (remove #(or (zero? %) (neg? %)))
                          count)
                     (if-not (:added-len env)
                       0
                       (* (:added-len env) (quot (:len env) (count dur)))))
                  16))
          dur (if-let [xtim (:xtim env)]
                (if (seqable? xtim)
                  (take (count dur) (cycle xtim))
                  (vec (repeat (count dur) xtim)))
                dur)
          dur (if-let [xtratim (:xtratim env)]
                (if (vector? xtratim)
                  (map #(* %1 %2) dur (cycle xtratim))
                  (map #(* % xtratim) dur))
                dur)
          env' (merge (nth instr 1) (nth instr 2) env)
          instr-indicies (:instr-indicies env)
          param-list (generate-p-keywords (:param-cnt (nth instr 1)))
          recurcion-level (determine-meta-recurcion env' param-list instr)]
      (loop [indx -1
             recurcion 0
             input-messages (vec (take recurcion-level (cycle '([]))))] 
        (let [recurcion (if (= (dec len) indx)
                          (inc recurcion) recurcion)
              indx (if (= (dec len) indx)
                     0 (inc indx))]
          (if-not (>= recurcion (max recurcion-level 1))
            (recur indx
                   recurcion
                   ;; Real parameter parsing starts here
                   (loop [param-keys param-list
                          params []]
                     ;; (prn "PARAMS: " params)
                     (if (empty? param-keys)
                       (if (= 0 recurcion-level)
                         (conj input-messages (apply (first instr) params))
                         (assoc input-messages recurcion
                                (conj (nth input-messages recurcion)
                                      (apply (first instr) params)))) 
                       (let [param-name (get (second instr) (first param-keys))
                             ;; _ (prn "KEYS: " (first param-keys) "ENV: " (second instr))
                             param-value (let [pmv (or (get-in env' param-name)
                                                       (get env' param-name))]
                                           (if-not (seqable? pmv)
                                             pmv
                                             (if (seqable? (first pmv))
                                               (nth pmv (mod recurcion (count pmv))) 
                                               pmv))) 
                             param-value (cond
                                           (= [:dur] param-name) dur
                                           (= [:freq] param-name) (let [freq param-value]
                                                                    ;; No frequency should be 0
                                                                    ;; use default instead.
                                                                    (if (some zero? freq)
                                                                      ;; replace all zeros
                                                                      (reduce #(conj %1 (if (zero? %2)
                                                                                          (:freq (nth instr 3)) %2)) [] freq)
                                                                      freq))
                                           :else param-value ;;(get env' param-name)
                                           )
                             value (if (number? param-value)
                                     param-value
                                     (if (seqable? param-value)
                                       (nth param-value (mod indx (count param-value)))
                                       param-value))
                             ;; _ (prn "PARAMNAME: " param-name "PARAMVAL: " param-value " VALUE: " value)
                             ]
                         (recur
                          (rest param-keys)
                          (into params [param-name value]))))))
            (let [input-messages (if (string? (first input-messages))
                                   (vector input-messages)
                                   input-messages)]
              ;; (prn "dur from parser fillbar: " (fill-the-bar (:dur env') (:len env')))
              (assoc env' :input-messages input-messages :dur (if (:uncycle? env')
                                                                (:dur env')
                                                                (fill-the-bar (:dur env') (:meter env'))))))))))


(defn ast-input-messages-builder [env instr]
  )



#_(apply (first (panaeolus.instruments.sampler/sampler :fx (panaeolus.fx/lofi)
                                                       )) [:dur 0.5 :amp -12 :freq 100])

#_(ast-input-messages-builder (panaeolus.algo.seq/seq {} '[1 2 3 4] 2 4) ;;(panaeolus.instruments.fof/priest)
                              (panaeolus.instruments.sampler/sampler)
                              ;; (panaeolus.instruments.fof/priest)
                              ;; (panaeolus.instruments.tr808/low_conga)
                              )

