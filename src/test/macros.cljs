(ns test.macros
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            ;;[panaeolus.macros :refer [seq]]
            ))

#_(deftest sequence-macro
    (is (= [1 1 1 -1] (:dur (seq {} [1 1 1 _]))))
    (is (= [1 1 1 1] (:dur (seq {} [1 1 1 1]))))
    (is (= [0.0625 0.0625 0.0625 0.0625] (:dur (seq {:grid 4} [x:4])))))

