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
        fx (if-not (empty? fx)
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
  [name instr & fx]
  (let [instr-number (or (get @csound-instrument-map name) 
                         (->> @csound-instrument-map
                              vals
                              (apply max)
                              inc))
        instr-string (replace-instr-number instr instr-number)
        instr-string (insert-zak-and-fx instr-string (first fx))]
    (.CompileOrc csound Csound instr-string)
    (swap! csound-instrument-map assoc name instr-number)
    instr-number))

;;(compile-csound-instrument "a" (fs/slurp "src/panaeolus/csound/orchestra/synth/nuclear.orc"))

(defn generate-p-keywords [p-count]
  (map #(keyword (str "p" %)) (range 3 (+ 3 p-count))))

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
        dur (if-let [d (:dur env)]
              (if (vector? d) d [d])
              ;; Should not be possible to reach this case.
              [1])
        len (+ (or (:len env) (count dur)) (or (:xtralen env) 0))
        ;; len (let [s (or (:len env) (count dur))]
        ;;       (if (zero? s) (* 4 (:meter env)) s))
        ;; dur (take len (cycle dur))
        dur (remove #(or (zero? %)
                         (neg? %)) dur)
        ;; dur (if number?
        ;;       (->> (cycle [p3]) (take len) vec)
        ;;       (->> (cycle p3) (take len) vec))
        dur (if-let [xtratim (:xtratim env)]
              (map #(* % xtratim) dur)
              dur)
        ;; param-keys (keys param-lookup-map)
        ;; group? (if-not (fn? input-msg-fn) true false)
        instr-indicies (:instr-indicies env)]
    (loop [indx 0
           input-messages []]
      (if-not (= len indx)
        (recur (inc indx)
               (let [instr-index (if (empty? instr-indicies)
                                   0
                                   (min instr-count
                                        (nth instr-indicies (mod indx (count instr-indicies)))))
                     instr' (nth instr instr-index) 
                     env' (merge (nth instr' 2) env)]
                 (loop [param-keys (keys (second instr'))
                        params []]
                   (if (empty? param-keys)
                     (conj input-messages (apply (first instr') params))
                     (let [param-name (get (second instr') (first param-keys))
                           param-value (if (= :dur param-name)
                                         dur (get env' param-name))
                           ;; POLYPHONY COULD BE ADDED HERE
                           value (if (number? param-value)
                                   param-value
                                   (nth param-value (mod indx (count param-value))))]
                       (recur
                        (rest param-keys)
                        (into params [param-name value])))))))
        (assoc env :input-messages input-messages :dur (fill-the-bar (:dur env) (:len env)))))))


;; (ast-input-messages-builder (panaeolus.algo.seq/seq {} '[x:2 x _ x]) (panaeolus.instruments.tr808/low_conga))

