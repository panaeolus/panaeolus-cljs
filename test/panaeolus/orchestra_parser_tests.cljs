(ns panaeolus.orchestra-parser-tests
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [panaeolus.orchestra-parser :refer [generate-p-keywords
                                                fold-hashmap]]))

(deftest generating-parameter-keywords
  (is (= '(:p3 :p4 :p5) (generate-p-keywords 3))))

(deftest folding-hash-map
  (is (= (fold-hashmap {:p4 {:amp -10} :p5 {:freq 110} :p6 {:rvb 1}})
         {:p4 :amp :p5 :freq :p6 :rvb})))



