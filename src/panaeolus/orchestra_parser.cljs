(ns panaeolus.orchestra-parser
  (:require [panaeolus.engine :as engine]
            [csound-wasm.public :as csound]
            [clojure.string :as string]
            ["fs" :as fs]))

#_(println (first (insert-zak-and-fx (fs/slurp "src/panaeolus/csound/orchestra/drone/sruti.csd")
                                     (panaeolus.fx/lofi))))
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
    ;; REMINDER, fix this regex
    [(string/replace instr #"\bout?.*" fx-and-zak) env]))

(defn- replace-instr-number [instr num]
  (string/replace-first instr #"instr\s+[0-9]*" (str "instr " num)))

(defn compile-csound-instrument
  "name is the function name for the instr
   instr is the csound slurp of the instr definition."
  [name instr env]
  ;; (prn name instr env)
  (let [name (if-let [pat-name (:pattern-name env)] 
               (str name pat-name)
               name)]
    ;; (prn "KEYS: " (keys env))
    ;; (prn "FX ENV: " (:fx env))
    (if (contains? @csound-instrument-map name)
      (let [instr-number (get @csound-instrument-map name)
            instr-string (replace-instr-number instr instr-number)
            [instr-string env] (insert-zak-and-fx instr-string env)
            ;; _ (prn instr-string)
            env (assoc env :recompile-fn (fn [] (engine/compile-orc instr-string)))]
        [instr-number env])
      (let [instr-number (->> @csound-instrument-map
                              vals
                              (apply max)
                              inc)
            _ (println "Csound instrument: " name "loaded.")
            instr-string (replace-instr-number instr instr-number)
            [instr-string env] (insert-zak-and-fx instr-string env)
            env (assoc env :recompile-fn (fn [] (engine/compile-orc instr-string)))
            ;;_ (prn "compiled-env2: " env)
            ]
        ;; (.CompileOrc csound Csound instr-string)
        (swap! csound-instrument-map assoc name instr-number)
        [instr-number env]))))

;; (compile-csound-instrument "a" (fs/slurp "src/panaeolus/csound/orchestra/synth/nuclear.orc") [])
