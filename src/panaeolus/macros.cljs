(ns panaeolus.macros$macros
  (:require [lumo.repl :refer [get-current-ns]]
            [macchiato.fs :as fs]
            [cljs.env :as env]
            [cljs.js :as cljs]
            [goog.string :as gstring]
            [clojure.string :as string]
            [panaeolus.engine :refer [csound Csound pattern-registry]]
            [panaeolus.orchestra-parser :as p]
            [panaeolus.broker :refer [pattern-loop-queue]])
  (:import [goog.string.isNumeric]
           [goog.string.toNumber]))


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
                     (if-let [fun# (get-in ~p-fields [param# :fn])]
                       (str " " (fun# final-env#))
                       (str " " (-> param#
                                    param-lookup-map#
                                    final-env#)))))))
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
             ;; env# ~env 
             ast# (p/ast-input-messages-builder ~env instr#)]
         (pattern-loop-queue (merge (nth instr# 2)
                                    ast# {:pattern-name ~(str pattern-name)
                                          :recompile-fn (nth instr# 3)}))))))

(defmacro seq
  "Parses a drum sequence, a _ symbol
   becomes a rest. Every tick in a sequence
   is equal in time. Numbers represent instrument
   group index or sample bank midi."
  [env v & grid+len]
  (if (= cljs.core/List (type v)) nil
      #_(if `(contains? ~env :bank)
          `(assoc ~env :dur ~dur :freq ~indx :seq-parsed? true)
          `(assoc ~env :dur ~dur :instr-indicies ~indx :seq-parsed? true))
      (let [[grid len] grid+len
            grid `(/ 1 (or (:grid ~env) ~grid 1))
            len `(or (:len ~env) ~len 16)]
        (loop [v v
               indx []
               dur []
               added-len 0]
          (if-not (empty? v)
            (let [next-symbol (first v)
                  [next-symbol extra] (if-not (symbol? next-symbol)
                                        [next-symbol nil]
                                        (let [nxs (str next-symbol)]
                                          (if (re-find #":" nxs)
                                            (string/split nxs ":")
                                            [next-symbol nil])))
                  rest? (or (= '_ next-symbol) (and (number? next-symbol)
                                                    (neg? next-symbol)))
                  key? (keyword? next-symbol)
                  key-is-numeric? (if-not
                                      key? nil
                                      (if (goog.string.isNumeric (name next-symbol))
                                        (goog.string.toNumber (name next-symbol))
                                        false))]
              (recur (if extra
                       (into [(keyword extra)] (rest v))
                       (rest v))
                     (cond 
                       rest? indx
                       key? (if key-is-numeric?
                              (into (subvec indx 0 (dec (count indx)))
                                    (repeat key-is-numeric? (last indx)))
                              ;; ADD SCALE LOOKUP ETC HERE
                              (conj indx next-symbol))
                       :else (if (integer? (first v))
                               (conj indx (Math/abs (first v)))
                               (conj indx 0)))
                     (if rest?
                       (conj dur `(* -1 ~grid))
                       (if key-is-numeric?
                         (into (subvec dur 0 (dec (count dur)))
                               (repeat key-is-numeric?
                                       `(/ (last ~dur) ~key-is-numeric?)))
                         (conj dur grid)))
                     (if-not (nil? extra)
                       (+ added-len (max 0 (dec (goog.string.toNumber extra))))
                       (if key-is-numeric?
                         (+ added-len (max 0 (dec key-is-numeric?)))
                         added-len))))
            (if `(contains? ~env :bank)
              `(assoc ~env :dur ~dur :freq ~indx :seq-parsed? true :len (+ ~added-len ~len))
              `(assoc ~env :dur ~dur :instr-indicies ~indx :seq-parsed? true :len (+ ~added-len ~len))))))))

;; (panaeolus.macros$macros/seq {} (vec (doall (range 0 10))) true)


;; (panaeolus.macros$macros/seq {:grid 4 :len 16} [67 67 67 67:4] 16)

(comment
  (panaeolus.macros/pat ::a (panaeolus.instruments.tr808/low_conga :amp -10)
                        ;; (fn [env] {}) 
                        (panaeolus.macros/seq [0 _ 1 _ 0 _ 2 _ 1])
                        ;; (assoc :dur [1 1 0.5 -0.25 -0.25])
                        
                        
                        ;; (assoc :kill true)
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

