(ns panaeolus.algo.control
  (:require
   [cljs.core.async :refer [>!]]
   [panaeolus.engine :refer [pattern-registry]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:import [cljs.core.async.impl.channels]))

(defn kill [env-or-patname]
  (if (map? env-or-patname)
    (assoc env-or-patname :kill true)
    (go (when-let [poll-channel (get @pattern-registry env-or-patname)]
          (>! poll-channel {:kill true}))
        (swap! pattern-registry dissoc env-or-patname))))

(defn killall []
  (go (loop [c (vals (dissoc @pattern-registry :forever))]
        (if (empty? c)
          (reset! pattern-registry (select-keys @pattern-registry [:forever]))
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

(defn xtim [env xtim]
  (assoc env :p3 xtim))

(defn louder [env]
  (assoc env :amp (let [amp (:amp env)]
                    (if (seqable? amp)
                      (mapv #(+ % 3.5) amp)
                      (+ 3.5 amp)))))

(defn quieter [env]
  (assoc env :amp (let [amp (:amp env)]
                    (if (seqable? amp)
                      (mapv #(- % 3.5) amp)
                      (- 3.5 amp)))))

