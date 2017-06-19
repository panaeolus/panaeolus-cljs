(ns panaeolus.instruments.sampler
  (:require [macchiato.fs :as fs]
            [clojure.string :as string]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [Csound csound]]
            [panaeolus.freq :refer [midi->freq freq->midi]]
            [panaeolus.fx :refer [freeverb lofi perc vibrato distort]])
  (:require-macros [panaeolus.macros :refer [definstrument demo]]))

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
  (if (contains? env [:bank])
    (loop [bank (let [b (get env [:bank])]
                  (if (or (list? b) (vector? b))
                    b [b]))
           total-bank []]
      (if (empty? bank)
        (freq-to-sample-num (or (get env [:midi]) (get env [:freq]))
                            0 total-bank)
        (recur
         (rest bank)
         (into total-bank
               (case (first bank)
                 ("stl-kick" :stl-kick) (get all-samples :stl_kicks)
                 ("stl-perc" :stl-perc) (get all-samples :stl_percs)
                 ("stl-bass" :stl-bass) (get all-samples :stl_bass)
                 ("stl-snare" :stl-snare) (get all-samples :stl_snares)
                 ("stl-rise" :stl-rise) (get all-samples :stl_rise)
                 ("stl-texture" :stl-texture) (get all-samples :stl_textures)
                 ("stl-clap" :stl-clap) (get all-samples :stl_clap)
                 ("stl-hat" :stl-hat) (get all-samples :stl_hats)
                 ("stl-fx" :stl-fx) (get all-samples :stl_fx)
                 ("pan" :pan) (get all-samples :pan)
                 ("jb" :jb) (get all-samples :jb)
                 ("jx" :jx) (get all-samples :jx)
                 ("jv" :jv) (get all-samples :jvgabba)
                 
                 ;; ("xx" :xx) (get all-samples :xx)
                 ;; ("pulse" :pulse) (get all-samples :pulse)
                 ;; ("rash" :rash) (get all-samples :rash) 
                 (throw (js/Error. (str "The sample bank: " bank " does not exist"))))))))
    (max 1000 (get env [:sample]))))

(definstrument "sampler"
  (fs/slurp "src/panaeolus/csound/orchestra/sampler/sampler.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:speed 1}
   :p6 {:freq 1000 :fn panaeolus.instruments.sampler/sampler-fn}
   :p7 {:loop 0}})

;; (definstrument "sampler2"
;;   (fs/slurp "src/panaeolus/csound/orchestra/sampler/sampler.orc")
;;   {:p3 {:dur 1}
;;    :p4 {:amp -12}
;;    :p5 {:speed 1}
;;    :p6 {:freq panaeolus.instruments.sampler/sampler-fn}
;;    :p7 {:loop 0}})

(definstrument "nsampler"
  (fs/slurp "src/panaeolus/csound/orchestra/sampler/nsampler.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 440}
   :p6 {:sample 1104}
   :p7 {:samplefreq 130.82}})

;; Hardcoded sample instruments
;; TODO add these to a private config.

(definstrument "stl-kicks"
  ;; 17 Samples
  (fs/slurp "src/panaeolus/csound/orchestra/sampler/sampler.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:speed 1}
   :p6 {:freq (fn [env]
                (let [tbl-num-v (get all-samples :stl_kicks)]
                  (nth tbl-num-v
                       (mod (:freq env) (count tbl-num-v)))))}
   :p7 {:loop 0}})


(definstrument "stl-synth"
  ;; 30 samples
  (fs/slurp "src/panaeolus/csound/orchestra/sampler/nsampler.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 440}
   :p6 {:sample (fn [env]
                  (let [tbl-num-v (get all-samples :stl_synths)]
                    (nth tbl-num-v
                         (mod (:sample env) (count tbl-num-v)))))}
   :p7 {:samplefreq 130.82}})

(definstrument "stl-bass"
  ;; 30 samples
  (fs/slurp "src/panaeolus/csound/orchestra/sampler/nsampler.orc")
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 440}
   :p6 {:sample (fn [env]
                  (let [tbl-num-v (get all-samples :stl_bass)]
                    (nth tbl-num-v
                         (mod (:sample env) (count tbl-num-v)))))}
   :p7 {:samplefreq 32.703}})

(comment 
  (demo (nsampler :amp -12 :freq 120 :sample 1135 :fx []))

  (demo (stl-synths :sample 20 :freq 400))

  (demo (sampler :bank "stl-texture" :freq 14
                 :fx [(distort)]
                 ))

  (count (get all-samples :stl_synths)))
