(ns panaeolus.macros$macros
  (:require [lumo.repl :refer [get-current-ns]]
            [macchiato.fs :as fs]
            [cljs.env :as env]
            [cljs.js :as cljs]
            [panaeolus.engine :refer [csound Csound pattern-registry]]
            [panaeolus.orchestra-parser :as p]
            [panaeolus.broker :refer [pattern-loop-queue]]))

;; (ns panaeolus.macros)

(defmacro pull-symbols [from-namespace]
  `(let [into-namespace# (symbol (lumo.repl/get-current-ns))
         into-map# (or (get-in @env/*compiler* [:cljs.analyzer/namespaces
                                                into-namespace#
                                                :uses]) {})
         new-symbols# (keys (get-in @env/*compiler*
                                    [:cljs.analyzer/namespaces ~from-namespace :defs]))
         merged-map# (reduce #(assoc %1 %2 ~from-namespace) into-map# new-symbols#)]
     (swap! env/*compiler* assoc-in [:cljs.analyzer/namespaces
                                     into-namespace#
                                     :uses] merged-map#)
     nil))


(defmacro pull-macros [from-namespace]
  `(let [into-namespace# (symbol (lumo.repl/get-current-ns))
         new-macros# (get-in @env/*compiler*
                             [:cljs.analyzer/namespaces ~from-namespace :use-macros])]
     (swap! env/*compiler* assoc-in [:cljs.analyzer/namespaces
                                     into-namespace#
                                     :use-macros] new-macros#)
     nil))

;; Lata þennan returne-a líka fx
(defmacro definstrument [instr-name csound-string p-fields]
  `(let [keys-vector# (into [(symbol "dur") (symbol "amp") (symbol "freq")] 
                            (->> ~p-fields
                                 vals
                                 (map keys)
                                 (map (comp symbol name first))))
         keys-vector# (-> keys-vector# distinct) 
         or-map# (merge {:dur 0.5}
                        (apply merge (vals ~p-fields))) 
         instr-number# (p/compile-csound-instrument
                        ~instr-name ~csound-string)
         param-lookup-map# (p/fold-hashmap ~p-fields)]
     (defn ~(symbol instr-name) 
       [~(symbol "&") {~(symbol "keys") keys-vector#
                       :or or-map#
                       :as env#}]
       [(fn [~(symbol "&") {~(symbol "keys") keys-vector#
                            :as closure-env#}]
          (let [p-count# (count (keys or-map#))
                final-env# (merge or-map# env# closure-env#)]
            (apply str "i " (or (:p1 final-env#) instr-number#) " 0"
                   (for [param# (p/generate-p-keywords
                                 p-count#)]
                     (str " " (-> param#
                                  param-lookup-map#
                                  final-env#))))))
        param-lookup-map#
        (merge or-map# env#)
        ;; recompile-fn
        (fn [] (p/compile-csound-instrument
                ~instr-name ~csound-string (:fx env#)))
        instr-number#])))

(defmacro demo [instr & dur]
  `(let [instr# ~instr
         dur# (or ~(first dur) 5)]
     ;; Always recompile demo
     ((nth instr# 3))
     (if (or (vector? (first instr#))
             (list? (first instr#)))
       (run! #(.InputMessage csound Csound %) (map #((first %) :dur dur#) instr#))
       (.InputMessage csound Csound ((first instr#) :dur dur#)))))

(defmacro forever [instr & dur]
  `(let [instr# ~instr
         dur# (or ~(first dur) 5)
         p1# (str (last instr#) ".001")]
     (if (or (vector? (first instr#))
             (list? (first instr#)))
       (println "Only single instruments allowed.")
       (if-let [instrnum# (some #{p1#} (:forever @pattern-registry))]
         (do
           (swap! pattern-registry assoc :forever (disj (:forever @pattern-registry) p1#))
           (.InputMessage csound Csound ((first instr#) :dur dur# :p1 (str "-" instrnum#))))
         (do
           (swap! pattern-registry assoc :forever (conj (:forever @pattern-registry) p1#))
           (.InputMessage csound Csound (let [f# ((first instr#) :dur (str "-" dur#) :p1 p1#)]
                                          f#)))))))

(defmacro pat [pattern-name instr & forms] 
  (loop [env {}, forms forms]
    (if forms
      (let [form (first forms)
            threaded (if (seq? form)
                       (with-meta `(~(first form) ~env ~@(next form)) (meta form))
                       (list form env))]
        (recur threaded (next forms)))        
      `(let [instr# ~instr
             env# ~env 
             ast# (p/ast-input-messages-builder env# instr#)]
         (pattern-loop-queue (merge (nth instr# 2)
                                    ast# {:pattern-name ~(str pattern-name)
                                          :recompile-fn (nth instr# 3)}))))))

(defmacro seq
  "Parses a drum sequence, a _ symbol
   becomes a rest. Every tick in a sequence
   is equal in time. Numbers represent instrument
   group index."
  [env v]
  (let [grid `(/ 1 (or (:grid ~env) 1))]
    (loop [v v
           indx []
           dur []]
      (if-not (empty? v)
        (let [rest? (= '_ (first v))]
          (recur (rest v)
                 (if rest?
                   indx
                   (if (integer? (first v))
                     (conj indx (Math/abs (first v)))
                     (conj indx 0)))
                 (if rest?
                   (conj dur `(* -1 ~grid))
                   (conj dur grid))))
        `(assoc ~env :dur ~dur :instr-indicies ~indx :seq-parsed? true)))))





(comment
  (panaeolus.macros/pat-> ::a (group (panaeolus.instruments.tr808/low_conga :amp -10)
                                     (panaeolus.instruments.tr808/mid_conga :amp -10)
                                     (panaeolus.instruments.tr808/high_conga :amp -10))
                          ;; (fn [env] {}) 
                          (panaeolus.macros/seq [0 _ 1 _ 0 _ 2 _ 1])
                          ;; (assoc :dur [1 1 0.5 -0.25 -0.25])
                          
                          
                          (assoc :kill true)
                          )

  (panaeolus.macros/pat-> ::a (panaeolus.instruments.tr808/low_conga :amp -10)
                          (assoc :dur [1 1 0.5 -0.25 -0.25])
                          ;; (assoc :kill true)
                          )

  (apply -> (list 1 inc))

  (comp {:a 1} (get % :a))
  
  (panaeolus.macros/pat-> ::a (test3 :dur [0.5 0.25 0.25 -0.25] :amp 1)
                          ;; (assoc :dur [1 1 0.5 0.25 -0.25])
                          ;; (assoc :len 16)
                          ;; (assoc :kill true)
                          )
  
  (fn? (test3))

  (panaeolus.macros/demo (panaeolus.instruments.tr808/low_conga :amp 0)))

