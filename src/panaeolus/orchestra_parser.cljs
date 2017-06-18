(ns panaeolus.orchestra-parser
  (:require [panaeolus.engine :refer [csound Csound]]
            [clojure.string :as string]
            [macchiato.fs :as fs]))

#_(insert-zak-and-fx (fs/slurp "src/panaeolus/csound/orchestra/synth/nuclear.orc")
                     (panaeolus.fx/lofi))
#_(println (insert-zak-and-fx
            (fs/slurp "src/panaeolus/csound/orchestra/synth/nuclear.orc")
            (panaeolus.fx/freeverb) 
            ))

(def csound-instrument-map
  "Map instr-name to instr-number"
  (atom {"%taken%" 1}))

(defn- determine-outs [instr]
  (let [instr-char-len (count instr)]
    (loop [indx (+ 5 (.indexOf instr "outs"))
           audio-vars ""]
      (let [char (get instr indx)]
        (if (or (= "\n" char) (= indx instr-char-len))
          (mapv string/trim (string/split audio-vars #","))
          (recur (inc indx) (str audio-vars char)))))))

(defn- insert-zak-and-fx [instr env]
  (let [[aL aR] (determine-outs instr)
        zak-system (str "chnmix " aL ",\"OutL\" \n"
                        "chnmix " aR ",\"OutR\" \n")
        fx (:fx env)
        ;; _ (prn "PARAM CNT: " (:param-cnt env))
        [fx env] (if-not (nil? fx)
                   (loop [fx-v (if (fn? fx)
                                 [fx] fx)
                          fx-str ""
                          env (assoc env :param-cnt (+ 2 (:param-cnt env)))]
                     (if (empty? fx-v)
                       [fx-str (assoc env :param-cnt (- (:param-cnt env) 2))]
                       (let [cur-fx (first fx-v)
                             [fx-str-res env-res] (cur-fx aL aR (:param-cnt env))]
                         (recur (rest fx-v)
                                (str fx-str fx-str-res)
                                (merge env env-res))))) 
                   ["" env])
        fx-and-zak (str fx "\n" zak-system)]
    [(string/replace instr #"\bout?.*" fx-and-zak) env]))

(defn- replace-instr-number [instr num]
  (string/replace instr #"instr\s+[0-9]*" (str "instr " num)))

(defn compile-csound-instrument
  "name is the function name for the instr
   instr is the csound slurp of the instr definition."
  [name instr env]
  (let [name (if-let [pat-name (:pattern-name env)] 
               (str name pat-name)
               name)]
    ;; (prn "KEYS: " (keys env))
    ;; (prn "FX ENV: " (:fx env))
    (if (contains? @csound-instrument-map name)
      (let [instr-number (get @csound-instrument-map name)
            instr-string (replace-instr-number instr instr-number)
            [instr-string env] (insert-zak-and-fx instr-string env)
            env (assoc env :recompile-fn (fn [] (.CompileOrc csound Csound instr-string)))]
        (if (not= (str (:fx env)) (get-in @csound-instrument-map (str name "-fx"))) 
          (swap! csound-instrument-map assoc (str name "-fx") (str (:fx env)))
          [instr-number env])
        [instr-number env])
      (let [instr-number (->> @csound-instrument-map
                              vals
                              (apply max)
                              inc)
            instr-string (replace-instr-number instr instr-number)
            [instr-string env] (insert-zak-and-fx instr-string env)
            env (assoc env :recompile-fn (fn [] (.CompileOrc csound Csound instr-string)))
            ;;_ (prn "compiled-env2: " env)
            ]
        ;; (.CompileOrc csound Csound instr-string)
        (swap! csound-instrument-map assoc name instr-number (str name "-fx") (str (:fx env)))
        [instr-number env]))))

;; (compile-csound-instrument "a" (fs/slurp "src/panaeolus/csound/orchestra/synth/nuclear.orc") [])

(defn generate-p-keywords [p-count]
  (map #(keyword (str "p" %)) (range 3 (+ p-count 3))))

;; (generate-p-keywords 8)
;; Used by definst in macros
(defn fold-hashmap [h-map]
  (reduce-kv #(assoc %1 %2 (if (vector? %3)
                             %3 [(first (keys %3))])) {} h-map))

;; Todo, create test for this
(defn fill-the-bar [v len]
  (let [cnt-v (count v)]
    (loop [filled-v []
           sum 0
           indx 0]
      (let [next (nth v (mod indx cnt-v))]
        (if (< len (+ (Math/abs next) sum))
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

(defn ast-input-messages-builder [env instr]
  (let [;;_ (prn "AST-ENV!: " env "AST-INSTR: " instr)
        ;; instr (if (fn? (first instr))
        ;;         [instr] instr)
        ;; instr-count (count instr)
        dur' (if-let [d (:dur env)]
               (if (vector? d) d [d])
               ;; Should not be possible to reach this case.
               [1]) 
        dur (remove #(or (zero? %) (neg? %)) dur')
        len (if (:uncycle? env)
              (count dur)
              (if-let [len (:len env)]
                (+ (->> (vec (take (:len env) (cycle dur')))
                        (remove #(or (zero? %) (neg? %)))
                        count)
                   (or (* (:xtralen env) (quot (:len env) (count dur))) 0))
                16)
              ;;(+ (count dur) (or (* (:xtralen env) (quot (:len env) (count dur))) 0))
              )
        ;; _ (prn (:len env) len)
        dur (if-let [xtim (:xtim env)]
              (if (seqable? xtim)
                (take (count dur) (cycle xtim))
                (vec (repeat (count dur) xtim)))
              dur)
        dur (if-let [xtratim (:xtratim env)]
              (map #(* % xtratim) dur)
              dur)
        env' (merge (nth instr 1) (nth instr 2) env)
        ;; _ (prn "ENV KOMMA: " env')
        instr-indicies (:instr-indicies env)
        param-list (generate-p-keywords (:param-cnt (nth instr 1)))
        ;; _ (prn "PARAM LIST" param-list)
        recurcion-level (determine-meta-recurcion env' param-list instr)]
    (loop [indx -1
           recurcion 0
           input-messages (vec (take recurcion-level (cycle '([]))))] 
      ;; (prn "INDEX: " input-messages "RL: " recurcion-level)
      (let [recurcion (if (= (dec len) indx)
                        (inc recurcion) recurcion)
            indx (if (= (dec len) indx)
                   0 (inc indx))]
        (if-not ;;(= len indx)
            (>= recurcion (max recurcion-level 1))
          ;; -1 ?          
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
                           param-value (let [pmv (get-in env' param-name)]
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
          (do ;;(prn input-messages)
            (assoc env' :input-messages input-messages :dur (if (:uncycle? env')
                                                              (:dur env')
                                                              (fill-the-bar (:dur env') (:len env'))))))))))


#_(apply (first (panaeolus.instruments.sampler/sampler :fx (panaeolus.fx/lofi)
                                                       )) [:dur 0.5 :amp -12 :freq 100])

#_(ast-input-messages-builder (panaeolus.algo.seq/seq {} '[1 2 3 4] 2 4) ;;(panaeolus.instruments.fof/priest)
                              (panaeolus.instruments.sampler/sampler)
                              ;; (panaeolus.instruments.fof/priest)
                              ;; (panaeolus.instruments.tr808/low_conga)
                              )
