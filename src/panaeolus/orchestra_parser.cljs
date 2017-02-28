(ns panaeolus.orchestra-parser
  (:require [macchiato.fs :as fs]
            [clojure.string :as string]))

(def csound-instrument-map
  "Map instr-name to instr-number"
  (atom {"test1" 2
         "test2" 3}))

(def ^:private low_conga
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/low_conga.orc"))

(defn- determine-outs [instr]
  (loop [indx (+ 5 (.indexOf instr "outs"))
         audio-vars ""]
    (let [char (get instr indx)]
      (if (or (= \newline char) (= indx (count csd-filestring)))
        (mapv string/trim (string/split audio-vars #","))
        (recur (inc indx) (str audio-vars char))))))


(defn- replace-instr-number [instr num]
  (string/replace instr #"instr\s+[0-9]*" num))



(defn compile-csound-instrument
  "name is the function name for the instr
   instr is the csound slurp of the instr definition."
  [name instr & fx]
  (let [instr-number (or (get @csound-instrument-map name) 
                         (->> @csound-instrument-map
                              vals
                              (apply max)
                              inc))]
    instr-number))


(compile-csound-instrument "test1" "")
