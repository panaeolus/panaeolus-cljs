(require '[cljs.test :refer [run-tests]]
         '[goog.object :as gobj]
         'panaeolus.broker-tests
         'panaeolus.orchestra-parser-tests)

(run-tests 'panaeolus.broker-tests)
(run-tests 'panaeolus.orchestra-parser-tests)

((gobj/get js/process "exit") 0)

