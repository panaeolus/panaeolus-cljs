(ns panaeolus.broker-tests
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [panaeolus.broker :refer [calc-mod-div
                                      create-event-queue
                                      calculate-timestamp]]))

(deftest calculate-mod-div
  (is (= 4 (calc-mod-div 4 [1 1 1 1])))
  (is (= 1 (calc-mod-div 0 [0.25 0.25 0.25 0.25]))))

(deftest event-queue
  (is (= #queue [[0 "i 1 0 1"] [1 "i 1 0 1"] [2 "i 1 0 1"] [3 "i 1 0 1"]]
         (create-event-queue [1 1 1 1] "i 1 0 1")))
  (is (= #queue [[0 "i 1 0 1"] [0.25 "i 1 1 1"] [0.5 "i 1 2 1"]]
         (create-event-queue [0.25 0.25 0.25] ["i 1 0 1" "i 1 1 1" "i 1 2 1"]))))

(deftest calculateing-timestamp
  (is (= 399.999 (calculate-timestamp 399.999 0 0)))
  (is (= 0 (calculate-timestamp 0 0 0))))

