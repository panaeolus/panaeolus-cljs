(ns panaeolus.macros
  (:require [lumo.repl :refer [get-current-ns]]
            [cljs.env :as env]
            [cljs.js :as cljs]
            [goog.string :as gstring]
            [clojure.string :as string]
            [panaeolus.engine :refer [csound Csound pattern-registry
                                      expand-home-dir slurp]]
            panaeolus.orchestra-parser
            [panaeolus.broker :refer [pattern-loop-queue]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defmacro pull-symbols [from-namespace into-namespace]
  `(let [;;into-namespace# (symbol (lumo.repl/get-current-ns))
         into-map# (or (get-in @env/*compiler* [:cljs.analyzer/namespaces
                                                ~into-namespace
                                                :uses]) {})
         new-symbols# (keys (get-in @env/*compiler*
                                    [:cljs.analyzer/namespaces ~from-namespace :defs]))
         merged-map# (reduce #(assoc %1 %2 ~from-namespace) into-map# new-symbols#)]
     (swap! env/*compiler* assoc-in [:cljs.analyzer/namespaces
                                     ~into-namespace
                                     :uses] merged-map#)
     nil))


(defmacro pull-macros [from-namespace into-namespace]
  `(let [;;into-namespace# (symbol (lumo.repl/get-current-ns))
         new-macros# (get-in @env/*compiler*
                             [:cljs.analyzer/namespaces ~from-namespace :use-macros])]
     (swap! env/*compiler* assoc-in [:cljs.analyzer/namespaces
                                     ~into-namespace
                                     :use-macros]
            (merge
             (or (get-in @cljs.env/*compiler*
                         [:cljs.analyzer/namespaces
                          ~into-namespace
                          :use-macros]) {})
             new-macros#))
     nil))


(defmacro definstrument [instr-name csound-file p-fields] 
  `(let [csound-string# (slurp (expand-home-dir ~csound-file))
         keys-vector# (into [(symbol "dur") (symbol "amp") (symbol "freq")] 
                            (->> ~p-fields
                                 vals
                                 (map keys)
                                 (map (comp symbol name first))))
         keys-vector# (-> keys-vector# distinct)
         or-map# (merge {:dur 0.5}
                        (apply merge (vals ~p-fields)))
         ;; IMPROVE: this literally breaks multiple :fn fields in instruments
         initial-param-cnt# (count (keys (dissoc or-map# :fn)))
         ;; Sends warning for wrong argument count, strange
         instr-number# (panaeolus.orchestra-parser/compile-csound-instrument
                        ~instr-name csound-string# {:param-cnt initial-param-cnt#})
         param-lookup-map# (panaeolus.orchestra-parser/fold-hashmap ~p-fields)
         ;; _# (prn "HEHEHR")
         ]
     (defn ~(symbol instr-name) 
       [~(symbol "&") {~(symbol "keys") keys-vector#
                       :or or-map#
                       :as instr-env#}]
       ;; recompile-fn
       ;; now implicitly recompile on every instr call
       (fn [pattern-name#]
         (let [[instr-number# env#] (panaeolus.orchestra-parser/compile-csound-instrument
                                     ~instr-name csound-string#
                                     (assoc instr-env# :param-cnt initial-param-cnt#
                                            :pattern-name pattern-name#))
               param-lookup-map# (merge param-lookup-map# (apply dissoc env# (keys instr-env#)))]
           
           [(fn [~(symbol "&") {~(symbol "keys") keys-vector#
                                :as closure-env#}]
              (let [
                  ;;;;;
                    p-count# (:param-cnt env#)
                    env-vector-formatted# (reduce-kv (fn [x# y# z#]
                                                       (assoc x# (if (vector? y#) y# [y#]) z#)) {}
                                                     (merge or-map# env#))
                    final-env# (merge env-vector-formatted# closure-env#)]
                (str "i " instr-number# " 0 "
                     (clojure.string/join " " (for [param# (panaeolus.orchestra-parser/generate-p-keywords
                                                            p-count#)]
                                                (if (contains? (param# ~p-fields) :fn)
                                                  ((get-in ~p-fields [param# :fn]) final-env#)
                                                  (get final-env# (get param-lookup-map# param#))))))))
            param-lookup-map#
            ;; Final env for instrument
            ;; are the two expressions the same?
            (merge or-map# instr-env# {:recompile-fn (:recompile-fn env#)})
            ;; Default param map
            or-map#
            instr-number#])))))

(defmacro define-fx
  [fx-name udo-file string-inject-fn param-vector]
  `(letfn [(param-key# [param-num#]
             (keyword (str "p" param-num#)))]
     (let [param-vector-keywords# (filter keyword? ~param-vector)           
           keys-vector# (->> param-vector-keywords#
                             (map (comp symbol name))
                             (into []))]
       (when ~udo-file
         (.CompileOrc csound Csound (slurp (expand-home-dir ~udo-file))))
       (defn ~(symbol fx-name)
         [~(symbol "&") {~(symbol "keys") keys-vector# :as fx-env#}]
         (let [fx-env# (merge (apply hash-map ~param-vector) fx-env#)]
           (fn [aL# aR# param-cnt#]
             (let [param-nums# (range (inc param-cnt#)
                                      (inc (+ param-cnt# (count param-vector-keywords#))))
                   param-val-list# (partition 2 (interleave (map param-key# param-nums#)
                                                            param-vector-keywords#))
                   fx-name-key# (keyword ~fx-name)]
               ;; Swap parameter name for p-field number
               [(apply ~string-inject-fn aL# aR#
                       (map (fn [s#] (str "p" s#)) param-nums#))
                ;; Make a map of {:px [:fx-name :param-name]}
                ;; and {[:fx-name :param-name] value}
                (merge (reduce (fn [init# val#] (assoc init# (first val#)
                                                       [fx-name-key# (second val#)]))
                               {} param-val-list#)
                       (reduce (fn [init# val#] (assoc init# [fx-name-key# (second val#)]
                                                       (get fx-env# (second val#))))
                               {} param-val-list#)
                       {:param-cnt (last param-nums#)})])))))))

(defmacro demo [instr & dur]
  `(let [instr# (~instr nil)
         dur# (or ~(first dur) 5)]
     ;; (prn "instr!!!!!: " ((first instr#) :dur dur#))
     ;; Always recompile demo
     ((:recompile-fn (nth instr# 2)))
     (if (or (vector? (first instr#))
             (list? (first instr#)))
       (run! #(.InputMessage csound Csound %) (map #((first %) [:dur] dur#) instr#))
       (.InputMessage csound Csound ((first instr#) :dur dur#)))))

(defmacro forever [instr & dur]
  `(let [instr# (~instr nil)
         dur# (or ~(first dur) 5)
         p1# (str (last instr#) ".001")]
     (if (or (vector? (first instr#))
             (list? (first instr#)))
       (println "Only single instruments allowed.")
       (if-let [instrnum# (some #{p1#} (:forever @pattern-registry))]
         (do
           (swap! pattern-registry assoc :forever (disj (:forever @pattern-registry) p1#))
           (.InputMessage csound Csound ((first instr#) [:dur] dur# ;;[:p1] (str "-" instrnum#)
                                         )))
         (do
           (swap! pattern-registry assoc :forever (conj (:forever @pattern-registry) p1#))
           (.InputMessage csound Csound (let [f# ((first instr#) [:dur] (str "-" dur#))]
                                          f#)))))))

(defmacro -> [& forms]
  (loop [x {}, forms forms]
    (if forms
      (let [form (first forms)
            threaded (if (seq? form)
                       (with-meta `(~(first form) ~x ~@(next form)) (meta form))
                       (list form x))]
        (recur threaded (next forms)))
      x)))
