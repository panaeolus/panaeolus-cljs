(ns panaeolus.algo.control
  (:require
   [cljs.core.async :refer [>!]]
   [panaeolus.engine :refer [bpm! pattern-registry]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:import [cljs.core.async.impl.channels]))

(defn kill [env]
  (assoc env :kill true))

(defn killall []
  (go (loop [c (vals (dissoc @pattern-registry :forever))]
        (if (empty? c)
          (reset! pattern-registry (select-keys @pattern-registry :forever))
          (do (>! (first c) {:kill true})
              (recur (rest c)))))))

(defn stop [env]
  (assoc env :stop? true))

(defn len [env length] 
  (assoc env :len length
         :dur (let [d (:dur env)]
                (if number? d) d
                (vec (take length (cycle d))))))

(defn meter [env meter]
  (assoc env :meter meter))

(defn grid
  "For sequencers, how much each beat
   is divided, defaults to 4 / 1 tick per beat.
   1=wholenote 2=halfnote 4=quavier 8=8th etc.."
  [env grid]
  (if (:seq-parsed? env)
    (assoc env :dur (mapv #(int (/ % (/ grid 4))) (or (:dur env) [])))
    (assoc env :grid (max (int (/ grid 4)) 1))))

(defn group
  "Group instruments togeather, in
   sequencers, the instrument identity
   becomes it's index position within
   the group."
  [& instruments]
  (vec instruments))


(defn xtratim [env xtratim]
  (assoc env :xtratim xtratim))
