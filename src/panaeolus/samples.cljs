(ns panaeolus.samples
  (:require [panaeolus.utils :as utils]
            [csound-wasm.node :as csound-node]
            [clojure.string :as string]
            [panaeolus.engine :as engine]
            [panaeolus.orchestra-init :refer [orc-init]]
            ["fs" :as fs]))

(def sample-directory
  (utils/expand-home-dir "~/.samples/"))

(def all-samples (atom {}))


;; For WASM the sample dir needs to be mounted

#_(when (= :wasm engine/csound-target)
  ;; This creates a virtual mounting point
  ;; not a real directory!
  (.mkdir js/FS "/samples")
  ;; This mounts the sample directory to WASM's root directory
  (.mount js/FS js/NODEFS #js {:root sample-directory} "./samples")

  nil)


(.mkdir csound-node/wasm-fs "/samples")

(.mount csound-node/wasm-fs csound-node/wasm-fs.filesystems.NODEFS #js {:root sample-directory} "./samples")


#_(engine/compile-orc csound (str "gi_ ftgen " "1007" ",0,0,1,\""
                                  ;; item-path
                                  #_(if (= :wasm engine/csound-target)
                                      wasm-path item-path)
                                  "./samples/xx/note-019.wav"
                                  "\",0,0,0\n"))

(js/setTimeout
 (fn []
   (let [root-dir (js->clj (fs/readdirSync sample-directory))
         root-path sample-directory]
     (loop [loop-v root-dir
            sub-dir [] 
            samples {}
            sample-num 1000]
       (if (empty? loop-v)
         (do (reset! all-samples samples)
             ;; (engine/input-message engine/csound "i 1 0 99999999")
             ;; Start the orchestra after sample load
             ;; (engine/wasm-start engine/csound-object engine/csound-instance)
             ;; (engine/input-message "i 10000 0 99999999999")
             #_(when (not= :udp engine/csound-target)
                 (println "Panaeolus loaded!\n")))
         (if (= "/" (last loop-v))
           (recur (subvec loop-v 0 (dec (count loop-v)))
                  (subvec sub-dir 0 (dec (count sub-dir)))
                  samples
                  sample-num)
           (let [item (last loop-v)
                 item-path (str root-path (apply str (interpose "/" sub-dir)) "/" item)
                 wasm-path (str "./samples/" (apply str (interpose "/" sub-dir)) "/" item)
                 item-is-directory? (.isDirectory (fs/lstatSync item-path))
                 item-is-wav? (if item-is-directory? nil
                                  (string/ends-with? item ".wav"))]
             (recur (if item-is-directory?
                      (into (conj (subvec loop-v 0 (dec (count loop-v))) "/")
                            (-> (fs/readdirSync item-path)
                                js->clj
                                sort reverse vec))
                      (subvec loop-v 0 (dec (count loop-v))))
                    (if item-is-directory?
                      (conj sub-dir item)
                      sub-dir)
                    (if (and (not item-is-directory?)
                             item-is-wav?)
                      (let [kw (keyword (last sub-dir))
                            orc-string (str "gi_ ftgen " sample-num ",0,0,1,\""
                                            ;; item-path
                                            wasm-path
                                            #_(if (= :wasm engine/csound-target)
                                                wasm-path item-path)
                                            "\",0,0,0\n")]
                        (engine/compile-orc orc-string)
                        #_(if (not= :udp engine/csound-target)
                            (engine/compile-orc orc-string)
                            (reset! engine/csound-udp-init-seq (update @engine/csound-udp-init-seq :samples conj orc-string)))
                        (assoc samples kw (if (contains? samples kw)
                                            (conj (kw samples) sample-num)
                                            [sample-num])))
                      samples)
                    (if (and (not item-is-directory?)
                             item-is-wav?)
                      (inc sample-num)
                      sample-num))))))))
 (if true;;(= :wasm engine/csound-target)
   1000 1))
