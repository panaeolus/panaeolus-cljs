(ns panaeolus.instruments.sampler
  (:require [macchiato.fs :as fs]
            [clojure.string :as string]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [Csound csound]]
            [panaeolus.macros :refer [definstrument demo pat seq]]
            [panaeolus.freq :refer [midi->freq freq->midi]]
            [panaeolus.fx :refer [freeverb lofi]]))

(def expand-home-dir (js/require "expand-home-dir"))

(def sample-directory (expand-home-dir "~/.samples/"))

(def all-samples
  (let [root-dir (fs/read-dir-sync sample-directory)
        root-path sample-directory]
    (loop [loop-v root-dir
           sub-dir [] 
           samples {}
           sample-num 1000]
      (if (empty? loop-v)
        samples
        (if (= "/" (last loop-v))
          (recur (subvec loop-v 0 (dec (count loop-v)))
                 (subvec sub-dir 0 (dec (count sub-dir)))
                 samples
                 sample-num)
          (let [item (last loop-v)
                item-path (str root-path (apply str (interpose "/" sub-dir)) "/" item)
                item-is-directory? (fs/directory? item-path)
                item-is-wav? (if item-is-directory? nil
                                 (string/ends-with? item ".wav"))]
            (recur (if item-is-directory?
                     (into (conj (subvec loop-v 0 (dec (count loop-v))) "/") (-> (fs/read-dir-sync item-path)
                                                                                 sort reverse vec))
                     (subvec loop-v 0 (dec (count loop-v))))
                   (if item-is-directory?
                     (conj sub-dir item)
                     sub-dir)
                   (if (and (not item-is-directory?)
                            item-is-wav?)
                     (let [kw (keyword (last sub-dir))]
                       (.CompileOrc csound Csound (str "gi_ ftgen " sample-num ",0,0,1,\""
                                                       item-path "\",0,0,0\n"))
                       (assoc samples kw (if (contains? samples kw)
                                           (conj (kw samples) sample-num)
                                           [sample-num])))
                     samples)
                   (if (and (not item-is-directory?)
                            item-is-wav?)
                     (inc sample-num)
                     sample-num))))))))

(defn freq-to-sample-num [freq midi-min bnk]
  (let [midi-max (+ (dec (count bnk)) midi-min)
        freq (if (vector? freq)
               (if (empty? freq) 0 (first freq))
               freq)
        freq (if-not freq 0
                     (if (< 127 freq)
                       (freq->midi freq) freq))]
    (if (or (< freq midi-min)
            (< midi-max freq))
      (get bnk (mod freq (count bnk)))
      (get bnk (mod (- freq midi-min) (count bnk))))))

(defn sampler-fn [env]
  ;; (prn env)
  (if-let [bank (:bank env)]
    (case bank
      ("jb" :jb) (freq-to-sample-num (or (:midi env) (:freq env))
                                     0 (get all-samples :jb))
      ("xx" :xx) (freq-to-sample-num (or (:midi env) (:freq env))
                                     12 (get all-samples :xx))
      ("jx" :jx) (freq-to-sample-num (or (:midi env) (:freq env))
                                     12 (get all-samples :jx))
      ("jv" :jv) (freq-to-sample-num (or (:midi env) (:freq env))
                                     35 (get all-samples :jvgabba))
      ("pulse" :pulse) (freq-to-sample-num (or (:midi env) (:freq env))
                                           36 (get all-samples :pulse))
      ("rash" :rash) (freq-to-sample-num (or (:midi env) (:freq env))
                                         36 (get all-samples :rash)) 
      (throw (js/Error. (str "The sample bank: " bank " does not exist"))))
    (max 1000 (:sample env))))

(definstrument "sampler"
  (fs/slurp "src/panaeolus/csound/orchestra/sampler/sampler.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:speed 1}
   :p6 {:freq 1000 :fn panaeolus.instruments.sampler/sampler-fn}
   :p7 {:loop 0}})

(definstrument "sampler2"
  (fs/slurp "src/panaeolus/csound/orchestra/sampler/sampler.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:speed 1}
   :p6 {:freq 1000 :fn panaeolus.instruments.sampler/sampler-fn}
   :p7 {:loop 0}})


;; (demo (sampler :speed 1 :amp -12 :bank "rash" :freq 1))



