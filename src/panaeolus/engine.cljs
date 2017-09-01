(ns panaeolus.engine
  (:require [cljs.core.async :as async
             :refer [<! >! chan timeout take! put!]]
            [panaeolus.orchestra-init :refer [orc-init]]
            [goog.object :as o]
            [macchiato.fs :as fs]
            libcsound)
  (:require-macros [cljs.core.async.macros
                    :refer [go go-loop]])
  (:import [goog.structs PriorityQueue]))


(def csound-target :wasm)

(def wasm-loaded-chan (chan 1))

(go (>! wasm-loaded-chan (fn? (.-_CsoundObj_new (.-asm js/Module)))))

(defn create-csound-object [target]
  (case target
    :wasm js/Module
    :native (js/require "csound-api")))

(defn create-csound-instance [target csound-object]
  (case target
    :wasm (._CsoundObj_new csound-object)
    :native (.Create csound-object)))

(def wasm-buffer-offset (volatile! 0))

(defn wasm-start [csound-object csound-instance]
  (.setFlagsFromString (js/require "v8") "--no-use_strict") ;; To be able to load web-audio-api
  (let [ksmps ((.cwrap csound-object "CsoundObj_getKsmps" #js ["number"] #js ["number"])
               csound-instance)
        _ (println "KSMPS = " ksmps)
        ;; (._CsoundObj_getKsmps csound-object csound-instance)
        input-count ((.cwrap csound-object "CsoundObj_getInputChannelCount" #js ["number"] #js ["number"])
                     csound-instance)
        ;; (._CsoundObj_getInputChannelCount csound-object csound-instance)
        output-count ((.cwrap csound-object "CsoundObj_getOutputChannelCount" #js ["number"] #js ["number"])
                      csound-instance)
        ;; (._CsoundObj_getOutputChannelCount csound-object csound-instance)
        audio-context-constructor (.-AudioContext (js/require "web-audio-api"))
        audio-context (new audio-context-constructor)
        audio-process-node (.createScriptProcessor
                            audio-context
                            1024 input-count output-count)
        _ (do (set! (.-inputCount audio-process-node) input-count)
              (set! (.-outputCount audio-process-node) output-count))
        buffer-size (.-bufferSize audio-process-node)
        output-pointer ((.cwrap csound-object "CsoundObj_getOutputBuffer" #js ["number"] #js ["number"])
                        csound-instance)
        ;; (._CsoundObj_getOutputBuffer csound-object csound-instance)
        csound-output-buffer (new js/Float32Array (.-buffer (.-HEAP8 csound-object))
                                  output-pointer (* ksmps output-count))
        ;; TODO add microphone input buffer
        zerodbfs ((.cwrap csound-object "CsoundObj_getZerodBFS" #js ["number"] #js ["number"])
                  csound-instance)
        ;; (._CsoundObj_getZerodBFS csound-object csound-instance)
        process-buffers (fn [e sample-count src-offset dst-offset]
                          (doseq [i (range output-count)]
                            (doseq [j (range sample-count)]
                              ;; (println i j output-count src-offset)
                              (aset (-> (.-outputBuffer e )
                                        (.getChannelData i))
                                    (+ j dst-offset)
                                    (aget csound-output-buffer
                                          (+ i (* output-count
                                                  (+ j src-offset))))))))
        ;; offset (atom ksmps)
        Speaker (js/require "speaker")
        perform-ksmps-fn (fn []
                           ((.cwrap csound-object "CsoundObj_performKsmps" #js ["number"] #js ["number"])
                            csound-instance
                            ;; (._CsoundObj_performKsmps csound-object csound-instance)
                            ))]
    (vreset! wasm-buffer-offset ksmps)
    (set! (.-outStream audio-context)
          (new Speaker #js {:channels (.-numberOfChannels
                                       (.-format audio-context))
                            :bitDepth (.-bitDepth
                                       (.-format audio-context))
                            :sampleRate (.-sampleRate audio-context)}))
    (.connect audio-process-node (.-destination audio-context))
    (set! (.-onaudioprocess audio-process-node)
          (fn [e]
            (let [sample-count (- ksmps @wasm-buffer-offset)
                  index (if (< 0 sample-count)
                          (do (process-buffers e sample-count @wasm-buffer-offset 0) sample-count)
                          0)]
              (loop [index index
                     sample-count sample-count]
                (if-not (< index buffer-size)
                  (vreset! wasm-buffer-offset sample-count)
                  (let [res (perform-ksmps-fn)
                        sample-count (Math/min ksmps (- buffer-size index))]
                    (if (not= 0 res)
                      (do (.disconnect audio-process-node)
                          (set! (.-onaudioprocess audio-process-node) nil))
                      (do (when (js/isNaN (aget csound-output-buffer 0))
                            (.error js/console (str "NaN! outputPointer = " output-pointer)))
                          (process-buffers e sample-count 0 index)
                          (recur (+ index sample-count)
                                 sample-count)))))))))
    nil))

(declare compile-orc
         get-current-time-samples
         input-message
         reset
         play
         set-option
         start)

(defprotocol CsoundInterface
  (compile-orc [this orc])
  (get-current-time-samples [this])
  (input-message [this input-message])
  (reset [this])
  (play [this])
  (set-option [this option])
  (start [this]))

(deftype CsoundWASM [csound-object csound-instance]
  CsoundInterface
  (compile-orc [this orc]
    ((.cwrap csound-object "CsoundObj_evaluateCode" "number" #js ["number" "string"])
     csound-instance orc))
  (input-message [this input-message]
    ((.cwrap csound-object "CsoundObj_readScore" "number" #js ["number" "string"])
     csound-instance input-message)
    ;; (._CsoundObj_readScore csound-object csound-instance input-message)
    )
  (reset [this] (._CsoundObj_reset csound-object csound-instance))
  (play [this] (._CsoundObj_play csound-object csound-instance))
  (set-option [this option] (._CsoundObj_setOption csound-object csound-instance option))
  (start [this]
    ((.cwrap csound-object "CsoundObj_prepareRT" nil #js ["number"])
     csound-instance)
    ((.cwrap csound-object "CsoundObj_compileOrc" "number" #js ["number" "string"])
     csound-instance "nchnls=2\n 0dbfs=1\n")
    ;;(._CsoundObj_compileOrc csound-object csound-instance "nchnls=2\n 0dbfs=1\n")
    (wasm-start csound-object csound-instance)
    ;;(._CsoundObj_play csound-object csound-instance)
    ))

(deftype CsoundNative [csound-object csound-instance]
  CsoundInterface
  (compile-orc [this orc]
    (.CompileOrc csound-object csound-instance orc))
  (get-current-time-samples [this]
    (.GetCurrentTimeSamples csound-object csound-instance))
  (input-message [this input-message]
    (.InputMessage csound-object csound-instance input-message))
  (reset [this] (.Reset csound-object csound-instance))
  (play [this] (.Play csound-object csound-instance))
  (set-option [this option] (.SetOption csound-object csound-instance option))
  (start [this]
    (.Start csound-object csound-instance)
    (.PerformAsync csound-object csound-instance
                   (fn [] (.Stop csound-object csound-instance)))))

;; (.setOption csound-object csound-instance "-odac")

(declare csound csound-object csound-instance)

(if (= :wasm csound-target)
  (go (<! wasm-loaded-chan)
      (println "Loaded libcsound.js")
      (def csound-object (create-csound-object csound-target))

      (def csound-instance (create-csound-instance panaeolus.engine/csound-target panaeolus.engine/csound-object))
      (def csound (case csound-target
                    :wasm (CsoundWASM. csound-object csound-instance)
                    :native (CsoundNative. csound-object csound-instance)))
      ;; (set-option csound "-odac")
      ;; (set-option csound "-d")
      (compile-orc csound orc-init)
      (start csound)
      ;; (input-message csound "i 10000 0 99999999999")
      )
  (do (def csound-object (create-csound-object csound-target))
      (def csound-instance (create-csound-instance panaeolus.engine/csound-target panaeolus.engine/csound-object))
      (def csound (case csound-target
                    :wasm (CsoundWASM. csound-object csound-instance)
                    :native (CsoundNative. csound-object csound-instance)))
      (set-option csound "-odac")
      (set-option csound "-d")
      (set-option csound "-m0")
      (compile-orc csound orc-init)
      (start csound)
      (input-message csound "i 10000 0 99999999999")))



(comment 
  (start csound)
  ;; (play csound)
  (compile-orc csound
               (str "instr 1\n"
                    "a1 poscil 0.9, 200\n"
                    "outs a1, a1\n"
                    "endin\n"))
  (input-message csound "f0 60")
  (input-message csound "i 1 0 1")
  (reset csound)
  (start csound))


(def expand-home-dir (js/require "expand-home-dir"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GLOBAL CHANNELS AND ATOMS ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def metro-channel (chan (async/sliding-buffer 1)))

(def poll-channel (chan (async/sliding-buffer 1024)))

(def pattern-registry (atom {:forever #{}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; METRONOME CLOCK CONTROLLER ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def bpm! nil)


;;;;;;;;;;;;;;;;;;;
;; Record audio ;;;
;;;;;;;;;;;;;;;;;;;

(def panaeolus-is-recording? (atom false))

(defn record! []
  (if @panaeolus-is-recording?
    (do (:input-message csound "i 9999 0 1 0")
        (println "\nRecording stopped!\n")
        (reset! panaeolus-is-recording? false))
    (do (:input-message csound "i 9999 0 1 1")
        (println "\nRecording started....\n")
        (reset! panaeolus-is-recording? true))))

;;;;;;;;;;;;;;;;;;;
;; Useful tools ;;;
;;;;;;;;;;;;;;;;;;;

(defn slurp [file]
  (fs/slurp file))

(defn csound-compile-file [file]
  (:compile-orc csound (fs/slurp (expand-home-dir file))))

