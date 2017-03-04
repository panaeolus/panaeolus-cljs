(ns panaeolus.macros$macros
  (:require [lumo.repl :refer [get-current-ns]]
            [macchiato.fs :as fs]
            [cljs.env :as env]
            [cljs.js :as cljs]
            [panaeolus.engine :refer [csound Csound]]
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
     (prn (str "nyja: " new-macros#))
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
       (fn [~(symbol "&") {~(symbol "keys") keys-vector#
                           :as closure-env#}]
         (let [p-count# (count (keys or-map#))
               final-env# (merge or-map# env# closure-env#)]
           (apply str "i " instr-number# " 0"
                  (for [param# (p/generate-p-keywords
                                p-count#)]
                    (str " " (-> param#
                                 param-lookup-map#
                                 final-env#)))))))))

(defmacro demo [instr]
  `(.InputMessage
    csound Csound (~instr :dur 5)))


(defmacro pat-> [name instr & forms]
  (loop [x {:input-messages instr}, forms forms]
    (if forms
      (let [form (first forms)
            threaded (if (seq? form)
                       (with-meta `(~(first form) ~x ~@(next form)) (meta form))
                       (list form x))]
        (recur threaded (next forms)))
      `(pattern-loop-queue (merge ~x {:pattern-name ~name 
                                      :meter 0})))))

(comment
  (panaeolus.macros/pat-> ::a "i 3 0 0.1 -3"
                          (assoc :dur [1 1 0.5 0.25 -0.25]))

  (apply -> (list 1 inc))

  (comp {:a 1} (get % :a))


  (panaeolus.macros/definstrument "test3" low_conga {:p3 {:dur 0.2}
                                                     :p4 {:amp -10}
                                                     :p5 {:freq 110}
                                                     :p6 {:rvb 1}})
  (fn? (test3))

  

  (panaeolus.macros/demo (panaeolus.instruments.tr808/low_conga :amp 0)))


(defn test4 [& {:keys [amp freq rvb]
                :or {:amp -10 :freq 110 :rvb 1}}]
  (str "i 3 0 " ))





