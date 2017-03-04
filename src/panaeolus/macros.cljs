(ns panaeolus.macros$macros
  (:require [lumo.repl :refer [get-current-ns]]
            [macchiato.fs :as fs]
            [cljs.env :as env]
            [panaeolus.engine :refer [csound Csound]]
            [panaeolus.orchestra-parser :as p]))

;; (ns panaeolus.macros)

(defmacro pull-symbols [from-namespace]
  `(let [into-namespace# (symbol (lumo.repl/get-current-ns))
         into-map# (or (get-in @env/*compiler* [:cljs.analyzer/namespaces
                                                into-namespace#
                                                :uses]) {})
         new-symbols# (keys (get-in @env/*compiler*
                                    [:cljs.analyzer/namespaces ~from-namespace :defs]))
         merged-map# (reduce #(assoc %1 %2 ~from-namespace) into-map# new-symbols#)]
     (prn into-namespace#)
     (swap! env/*compiler* assoc-in [:cljs.analyzer/namespaces
                                     into-namespace#
                                     :uses] merged-map#)
     nil))


(def ^:private low_conga
  (fs/slurp "src/panaeolus/csound/orchestra/tr808/low_conga.orc"))

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
         param-lookup-map# (p/fold-hashmap ~p-fields)
         ]
     (defn ~(symbol instr-name) 
       [~(symbol "&") {~(symbol "keys") keys-vector#
                       :or or-map#
                       :as env#}]
       (let [p-count# (count (keys or-map#))
             env# (merge or-map# env#)]
         (apply str "i " instr-number# " 0"
                (for [param# (p/generate-p-keywords
                              p-count#)]
                  (str " " (-> param#
                               param-lookup-map#
                               env#))))))))

(comment 
  (defn test4 [& {:keys [amp freq rvb]
                  :or {:amp -10 :freq 110 :rvb 1}}]
    (str "i 3 0 " ))

  (test3 :amp 100)

  (panaeolus.macros/definstrument "test3" low_conga {:p4 {:amp -10}
                                                     :p5 {:freq 110}
                                                     :p6 {:rvb 1}}))




