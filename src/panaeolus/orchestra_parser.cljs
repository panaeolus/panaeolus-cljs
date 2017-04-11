(ns panaeolus.orchestra-parser
  (:require [panaeolus.engine :refer [csound Csound]]
            [clojure.string :as string]
            [macchiato.fs :as fs]))

;; (insert-zak-system (fs/slurp "src/panaeolus/csound/orchestra/synth/nuclear.orc"))

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

(defn- insert-zak-and-fx [instr & fx]
  (let [[aL aR] (determine-outs instr)
        zak-system (str "chnmix " aL ",\"OutL\" \n"
                        "chnmix " aR ",\"OutR\" \n")
        fx (if-not (or (nil? fx) (empty? fx))
             (if (fn? (first fx))
               ((first fx) aL aR)
               (apply str (map #(% aL aR) (first fx))))
             "")
        fx-and-zak (str  fx "\n" zak-system)]
    (string/replace instr #"\bout?.*" fx-and-zak)))

(defn- replace-instr-number [instr num]
  (clojure.string/replace instr #"instr\s+[0-9]*" (str "instr " num)))


(defn compile-csound-instrument
  "name is the function name for the instr
   instr is the csound slurp of the instr definition."
  [name instr fx & pat-name]
  (let [name (if (first pat-name) 
               (str name (first pat-name))
               name)
        instr-number (or (get @csound-instrument-map name) 
                         (->> @csound-instrument-map
                              vals
                              (apply max)
                              inc))
        instr-string (replace-instr-number instr instr-number)
        instr-string (insert-zak-and-fx instr-string fx)]
    (.CompileOrc csound Csound instr-string)
    (swap! csound-instrument-map assoc name instr-number)
    instr-number))

;;(compile-csound-instrument "a" (fs/slurp "src/panaeolus/csound/orchestra/synth/nuclear.orc"))

(defn generate-p-keywords [p-count]
  (map #(keyword (str "p" %)) (range 3 (+ 3 p-count))))

;; Used by definst in macros
(defn fold-hashmap [h-map]
  (reduce-kv #(assoc %1 %2 (first (keys %3))) {} h-map))

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


;; Note to self
;; here the dur is really p3 but not
;; the duration between events.
(defn ast-input-messages-builder [env instr]
  (let [instr (if (fn? (first instr))
                [instr] instr)
        instr-count (count instr)
        dur' (if-let [d (:dur env)]
               (if (vector? d) d [d])
               ;; Should not be possible to reach this case.
               [1])
        dur (remove #(or (zero? %) (neg? %)) dur') 
        len (if (:uncycle? env)
              (count dur)
              (+ (->> (vec (take (:len env) (cycle dur')))
                      (remove #(or (zero? %) (neg? %)))
                      count)
                 (or (* (:xtralen env) (quot (:len env) (count dur))) 0))
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
        instr-indicies (:instr-indicies env)]
    ;; Instrument group parsing
    (loop [indx 0
           input-messages []]
      (prn "INDEX: " input-messages)
      (if-not (= len indx)
        (recur (inc indx) 
               (let [instr-index (if (empty? instr-indicies)
                                   0
                                   (min instr-count
                                        (nth instr-indicies (mod indx (count instr-indicies)))))
                     instr' (nth instr instr-index) 
                     env' (merge (nth instr' 2) env)]
                 ;; Real parameter parsing starts here
                 (loop [param-keys (keys (second instr'))
                        params []
                        nested-input-messages []
                        meta-pattern-nesting-cnt nil
                        meta-pattern-nesting-indx 0]
                   (if (empty? param-keys)
                     (if-not meta-pattern-nesting-cnt
                       (conj input-messages (apply (first instr') params))
                       (if (= (dec meta-pattern-nesting-indx) meta-pattern-nesting-cnt)
                         (conj input-messages nested-input-messages)
                         ;;(conj input-messages [(apply (first instr') params)])
                         (recur (keys (second instr'))
                                []
                                (conj nested-input-messages params)
                                meta-pattern-nesting-cnt
                                (inc meta-pattern-nesting-indx))))
                     (let [param-name (get (second instr') (first param-keys))
                           meta-pattern-nesting-cnt (if (pos? meta-pattern-nesting-indx)
                                                      meta-pattern-nesting-cnt
                                                      (if (vector? (first (get env' param-name)))
                                                        (max (count (get env' param-name)) meta-pattern-nesting-cnt)
                                                        meta-pattern-nesting-cnt)) 
                           param-value (if-not meta-pattern-nesting-cnt
                                         (get env' param-name)
                                         (let [pmv (get env' param-name)]
                                           (if (vector? (first pmv))
                                             (nth pmv (mod (count pmv) meta-pattern-nesting-cnt))
                                             pmv)))
                           _ (prn param-value indx)
                           param-value (cond
                                         (= :dur param-name) dur
                                         (= :freq param-name) (let [freq param-value]
                                                                ;; No frequency should be 0
                                                                ;; use default instead.
                                                                (if (some zero? freq)
                                                                  ;; replace all zeros
                                                                  (reduce #(conj %1 (if (zero? %2) (:freq (nth instr' 3)) %2)) [] freq)
                                                                  freq))
                                         :else param-value ;;(get env' param-name)
                                         )
                           ;; POLYPHONY COULD BE ADDED HERE
                           value (if (number? param-value)
                                   param-value
                                   (nth param-value (mod indx (count param-value))))]
                       (recur
                        (rest param-keys)
                        (into params [param-name value])
                        nested-input-messages
                        meta-pattern-nesting-cnt
                        meta-pattern-nesting-indx))))))
        (assoc env :input-messages input-messages :dur (if (:uncycle? env)
                                                         (:dur env)
                                                         (fill-the-bar (:dur env) (:len env))))))))


;; (ast-input-messages-builder (panaeolus.algo.seq/seq {:uncycle? false} '[3 5 3 5 3 5 3 5:] 2 16) (panaeolus.instruments.tr808/low_conga))
#_(ast-input-messages-builder (panaeolus.algo.seq/seq {:uncycle? false} '[10 20 _ 30] 1 8) (panaeolus.instruments.synths/sweet :amp [[-1 -2 -3]
                                                                                                                                     [-4 -5 -6]
                                                                                                                                     ]))

;; TODO make test if positiv :dur count equals to count of input-messages

