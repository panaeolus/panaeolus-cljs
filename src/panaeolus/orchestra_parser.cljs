(ns panaeolus.orchestra-parser
  (:require [macchiato.fs :as fs]
            [panaeolus.engine :refer [csound Csound]]
            [clojure.string :as string]))

(def csound-instrument-map
  "Map instr-name to instr-number"
  (atom {"%taken%" 1}))

(def ^:private low_conga
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/low_conga.orc"))

(defn- determine-outs [instr]
  (let [instr-char-len (count instr)]
    (loop [indx (+ 5 (.indexOf instr "outs"))
           audio-vars ""]
      (let [char (get instr indx)]
        (if (or (= "\n" char) (= indx instr-char-len))
          (mapv string/trim (string/split audio-vars #","))
          (recur (inc indx) (str audio-vars char)))))))

(defn- replace-instr-number [instr num]
  (string/replace instr #"instr\s+[0-9]*" (str "instr " num)))


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
    instr-string))


(defn definstrument [])
