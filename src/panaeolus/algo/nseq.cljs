(ns panaeolus.algo.nseq
  (:require [clojure.string :as string]
            [panaeolus.freq]
            [instaparse.core :as insta :refer-macros [defparser]])
  (:import [goog.string.isNumeric]
           [goog.string.toNumber]))


(def ^:private process-nname
  (comp panaeolus.freq/midi->freq
        panaeolus.freq/note->midi
        keyword))

(def ^:private process-nnum
  "All values under 127 are midi
   note numbers, otherwise Hz."
  (fn [nnum]
    (if (< nnum 127)
      (panaeolus.freq/midi->freq nnum)
      nnum)))


;; Generte [freq dur] pairs

(defn calculate-time-indicators [freq time]
  (let [time (if (nil? time)
               1 time)]
    (if-not (vector? time)
      [freq (goog.string.toNumber time)]
      (loop [tv (rest time) 
             div nil
             ext (if freq 1 -1)]
        (if (empty? tv)
          (if div
            (let [div (max (int (goog.string.toNumber div)) 1)]
              (if freq
                (doall (repeat div [freq (* ext (/ 1 div))]))
                ext))
            (if freq
              [freq ext]
              ext))
          (case (ffirst tv)
            :ext (recur (rest tv)
                        div
                        (* ext (->> tv first second goog.string.toNumber)))
            :div (recur (rest tv)
                        (-> tv first second)
                        ext)))))))


(defmulti nseq (fn [_ nseqv & grid+len]
                 (cond 
                   (string? nseqv) :parser
                   (vector? nseqv) :function
                   :else (throw (js/Error. (str "Missing string or vector in nseq"))))))

#_((insta/parser
    "<list> = token (<whitespace> token)*
   <token> = nname | nnum | rest | simile
   whitespace = #'\\s+'

   nname = (letter digit)+ time?
   nnum  = digit+ time?
   time = (div | ext)*

   div = <divided> digit+
   ext = <extended> digit+
   rest = <('_' | 'r' | 'R')>+ time?
   simile = <('/')>+

   <dotted> = '.'
   <divided> = ':'
   <extended> = '*'
   <octave> = ',' | '\\''    
   <letter> = #'[a-zA-Z]+'
   <digit> = #'[0-9]+\\.?[0-9]*'") "")


(defparser parser-nseq
  "<list> = token (<whitespace> token)*
   <token> = nname | nnum | rest | simile
   whitespace = #'\\s+'

   nname = (letter digit)+ time?
   nnum  = digit+ time?
   time = (div | ext)*

   div = <divided> digit+
   ext = <extended> digit+
   rest = <('_' | 'r' | 'R')>+ time?
   simile = <('/')>+

   <dotted> = '.'
   <divided> = ':'
   <extended> = '*'
   <octave> = ',' | '\\''    
   <letter> = #'[a-zA-Z]+'
   <digit> = #'[0-9]+\\.?[0-9]*'")


(defmethod nseq :parser
  [env nseq-s & grid+len]
  (let [[grid len] grid+len
        grid (/ 4 (or (:grid env) grid 4))
        ;; grid' (/ 1 (/ grid 4))
        len (or (:len env) len 16)
        res (->> nseq-s
                 parser-nseq
                 (insta/transform
                  {:nname (fn [nname octave time]
                            (let [freq (process-nname (str nname octave))
                                  freq+time (calculate-time-indicators freq time)]
                              freq+time))
                   :nnum (fn [nnum octave time]
                           (let [freq (process-nnum (str nnum octave))]
                             [freq time]))
                   :rest (fn [time]
                           (if-not time
                             -1
                             (calculate-time-indicators nil time)))}))]
    (loop [res res
           freq []
           dur []
           ;; len 0
           added-len 0]
      (if (empty? res)
        (assoc env :dur dur :freq freq :len len :seq-parsed? true
               :meter (* grid len))
        
        ;; Ask if list/lazy-seq (is lazy from repeat in div)
        ;; and add it to the front of the res sequence.
        (if (and (not (number? (first res)))
                 (not (vector? (first res))))
          (recur (apply conj (vec (first res)) (rest res))
                 freq dur (+ added-len (dec (count (first res)))))
          (if (vector? (first res))
            (recur (rest res)
                   (conj freq (ffirst res))
                   (conj dur (-> res first second))
                   ;; (inc len)
                   added-len)
            (recur (rest res)
                   freq
                   (conj dur (first res))
                   ;; len
                   added-len)))))))

;; (apply conj (vec '(1 1.5)) '(2 3))


(defmethod nseq :function
  [env nseqv & grid+len]
  "Parses a vector, allows an '_ symbol
   (rest). Every tick in a sequence
   is equal in time. Numbers represent instrument
   group index or sample bank midi."
  (when (some seqable? grid+len)
    (throw (js/Error. (str "ERROR IN SEQ"))))
  (let [[grid len] grid+len
        grid (/ 4 (or (:grid env) grid 4))
        ;; grid' (/ 1 (/ grid 4))
        len' (or (:len env) len 16)]
    (loop [v' nseqv
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
        (assoc env :dur dur :seq-parsed? true :added-len added-len :freq notenum :len len
               ;; For mod-div calculation
               :meter (* grid len)
               ;; :seq-v nseqv
               ;; :seq-grid grid
               ;; :seq-len len
               )))))


;; (nseq {} "as3*2.2 r d3:3 c3 r*3")
#_(panaeolus.orchestra-parser/ast-input-messages-builder (panaeolus.algo.nseq/nseq {} '[x _ x _ x _ 400 _ ] 4 8)
                                                         ((panaeolus.instruments.synths/sweet)))
;; (panaeolus.algo.nseq/nseq {} '[x _ x _ x _ 400 _ ] 0.5 8)
