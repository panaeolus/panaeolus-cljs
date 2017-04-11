(ns panaeolus.algo.seq
  (:require [clojure.string :as string]
            [panaeolus.freq])
  (:import [goog.string.isNumeric]
           [goog.string.toNumber]))


(defn seq
  "Parses a drum sequence, a '_ symbol
   becomes a rest. Every tick in a sequence
   is equal in time. Numbers represent instrument
   group index or sample bank midi."
  [env v & grid+len]
  (when (some seqable? grid+len)
    (throw (js/Error. (str "ERROR IN SEQ"))))
  (let [[grid' len'] grid+len
        grid (/ 1 (or (:grid env) grid' 1))
        len (or (:len env) len' 16)] 
    (loop [v' v
           notenum []
           dur []
           added-len 0]
      (if-not (empty? v')
        (let [fv (first v')
              ;; _ (prn fv)
              [fv extra] (if-not (symbol? fv)
                           [fv nil]
                           (let [fvs (str fv)]
                             (if (re-find #":" fvs)
                               (string/split fvs ":")
                               [fv nil])))
              rest? (cond
                      (= '_ fv) (* -1 grid)
                      (and (number? fv) (neg? fv)) fv
                      :else false)
              key? (keyword? fv)
              key-is-numeric? (if-not
                                  key? nil
                                  (if (goog.string.isNumeric (name fv))
                                    (goog.string.toNumber (name fv))
                                    false))]
          (recur ;; (rest v)
           (if extra
             (into [(keyword extra)] (rest v'))
             (rest v'))
           (cond 
             rest? notenum
             key?  (if key-is-numeric?
                     (into (subvec notenum 0 (dec (count notenum)))
                           (repeat key-is-numeric? (last notenum)))
                     ;; ADD SCALE LOOKUP ETC HERE
                     (conj notenum fv))
             :else (if (integer? fv)
                     (conj notenum (Math/abs fv))
                     (conj notenum 0)))
           ;; (if rest? num (conj num fv))
           (if rest? (conj dur rest?)
               (if key-is-numeric?
                 (into (subvec dur 0 (dec (count dur)))
                       (repeat key-is-numeric?
                               (/ (last dur) key-is-numeric?)))
                 (conj dur grid)))
           (if-not (nil? extra)
             (+ added-len (max 0 (dec (goog.string.toNumber extra))))
             (if key-is-numeric?
               (+ added-len (max 0 (dec key-is-numeric?)))
               added-len))))
        (assoc env :dur dur :seq-parsed? true :xtralen added-len :freq notenum :len len
               :seq-v v :seq-grid grid' :seq-len len')))))

;; (panaeolus.algo.seq/seq {} '[x _ x _ x _ x _ ] 1)


