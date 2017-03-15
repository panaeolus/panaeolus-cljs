(ns panaeolus.instruments.sampler
  (:require [macchiato.fs :as fs]
            [clojure.string :as string]
            [panaeolus.algo.control :refer [group]]
            [panaeolus.engine :refer [Csound csound]]
            [panaeolus.macros :refer [definstrument demo]]
            [panaeolus.fx :refer [freeverb lofi]]))


(def sample-directory "/home/hlolli/samples/")

(fs/directory? (first (fs/read-dir-sync "/home/hlolli/samples/")))

(let [root-dir (fs/read-dir-sync sample-directory)
      root-path sample-directory]
  (loop [loop-v root-dir
         sub-dir [] 
         samples []]
    (if (empty? loop-v)
      samples
      (if (= "/" (last loop-v))
        (recur (subvec loop-v 0 (dec (count loop-v)))
               (subvec sub-dir 0 (dec (count sub-dir)))
               samples)
        (let [item (last loop-v)
              item-path (str root-path (apply str (interpose "/" sub-dir)) "/" item)
              item-is-directory? (fs/directory? item-path)
              item-is-wav? (if item-is-directory? nil
                               (string/ends-with? item ".wav"))]
          (recur (if item-is-directory?
                   (into (conj (subvec loop-v 0 (dec (count loop-v))) "/") (fs/read-dir-sync item-path))
                   (subvec loop-v 0 (dec (count loop-v))))
                 (if item-is-directory?
                   (conj sub-dir item)
                   sub-dir)
                 (if (and (not item-is-directory?)
                          item-is-wav?)
                   (conj samples item-path)
                   samples)))))))
