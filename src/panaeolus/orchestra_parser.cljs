(ns panaeolus.orchestra-parser
  (:require [panaeolus.engine :refer [csound Csound]]
            [clojure.string :as string]
            [cljs.env :as env]))


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
        instr-string (replace-instr-number instr instr-number) ]
    (.CompileOrc csound Csound instr-string)
    (swap! csound-instrument-map assoc name instr-number)
    instr-number))

(defn generate-p-keywords [p-count]
  (map #(keyword (str "p" %)) (range 3 (+ 3 p-count))))

(defn fold-hashmap [h-map]
  (reduce-kv #(assoc %1 %2 (first (keys %3))) {} h-map))
(concat nil (list 5))

(defn ast-input-messages-builder [env instr]
  (let [instr (if (fn? (first instr))
                [instr] instr)
        instr-count (count instr)
        dur (if-let [d (:dur env)]
              (if (vector? d) d [d]) [1])
        dur (remove #(or (neg? %)
                         (zero? %)) dur)
        len (let [s (or (:len env) (count dur))]
              (if (zero? s) 16 s)) 
        dur (take len (cycle dur))
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
                     env' (merge env (nth instr' 2))]
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
        (assoc env :input-messages input-messages
               :dur (vec dur))))))

#_(get-in @env/*compiler* [:cljs.analyzer/namespaces
                           'panaeolus.orchestra-parser
                           :uses])
