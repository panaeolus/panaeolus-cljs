(ns panaeolus.instruments.sampler
  (:require [macchiato.fs :as fs]
            [clojure.string :as string]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [csound] :as engine]
            [panaeolus.freq :refer [midi->freq freq->midi]]
            [panaeolus.samples :refer [all-samples]])
  (:require-macros [panaeolus.macros :refer [definstrument demo]]))



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
               (or (get @all-samples (keyword (first bank)))
                   (get @all-samples (first bank))
                   (throw (js/Error. (str "The sample bank: " (first bank) " does not exist"
                                          "available banks are: " (keys @all-samples)))))))))
    (max 1000 (get env [:sample]))))

(definstrument "sampler"
  "src/panaeolus/csound/orchestra/sampler/sampler.orc"
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
  "src/panaeolus/csound/orchestra/sampler/nsampler.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 440}
   :p6 {:sample 1104}
   :p7 {:samplefreq 130.82}})

(definstrument "sample-stretch"
  "src/panaeolus/csound/orchestra/sampler/sample-stretch.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:nnop 440}
   :p6 {:freq 1000 :fn panaeolus.instruments.sampler/sampler-fn}
   :p7 {:stretch 30}})


;; Hardcoded sample instruments
;; TODO add these to a private config.

(definstrument "stl-kicks"
  ;; 17 Samples
  "src/panaeolus/csound/orchestra/sampler/sampler.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:speed 1}
   :p6 {:freq (first (get @all-samples :stl-kick))
        :fn (fn [env]
              (let [tbl-num-v (get @all-samples :stl-kick)]
                (nth tbl-num-v
                     (mod (get env [:freq]) (count tbl-num-v)))))}
   :p7 {:loop 0}})


(definstrument "stl-synth"
  ;; 30 samples
  "src/panaeolus/csound/orchestra/sampler/nsampler.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 440}
   :p6 {:sample 0 :fn (fn [env]
                        (let [tbl-num-v (get @all-samples :stl-synth)] 
                          (nth tbl-num-v
                               (mod (get env [:sample]) (count tbl-num-v)))))}
   :p7 {:samplefreq 130.82}})

(definstrument "stl-bass"
  ;; 30 samples
  "src/panaeolus/csound/orchestra/sampler/nsampler.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 440}
   :p6 {:sample 0 :fn (fn [env]
                        (let [tbl-num-v (get @all-samples :stl-bass)]
                          (nth tbl-num-v
                               (mod (get env [:sample]) (count tbl-num-v)))))}
   :p7 {:samplefreq 32.703}})


(comment 
  (demo (nsampler :amp -12 :freq 120 :sample 1135 :fx []))

  (demo (stl-synths :sample 20 :freq 400))

  (demo (sampler :bank "stl-texture" :freq 14
                 :fx [(distort)]
                 ))

  (count (get @all-samples :stl_synths)))
