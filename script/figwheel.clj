(require 
 '[figwheel-sidecar.repl-api :as ra]
 '[com.stuartsierra.component :as component]
 '[dev.handler])


(import 'java.lang.Runtime)

(def figwheel-config
  {:figwheel-options {:ring-handler "dev.handler/handler"
                      :server-port 8440
                      :css-dirs ["resources/public/css"]}
   :build-ids ["dev"]
   :all-builds
   [{:id "dev" 
     :figwheel true
     :source-paths ["src"]
     :compiler {:main "ui.main"
                :externs ["externs/ace.ext.js"]
                :foreign-libs [{:file "libs/ace/ace.js"
                                :provides ["ace"]}
                               {:file "libs/ace/theme-ambience.js"
                                :provides ["ace-theme-ambience"]
                                :requires ["ace"]}
                               {:file "libs/ace/theme-chaos.js"
                                :provides ["ace-theme-chaos"]
                                :requires ["ace"]}
                               {:file "libs/ace/theme-chrome.js"
                                :provides ["ace-theme-chrome"]
                                :requires ["ace"]}
                               {:file "libs/ace/theme-clouds.js"
                                :provides ["ace-theme-clouds"]
                                :requires ["ace"]}
                               {:file "libs/ace/theme-clouds_midnight.js"
                                :provides ["ace-theme-clouds-midnight"]
                                :requires ["ace"]}
                               {:file "libs/ace/theme-cobalt.js"
                                :provides ["ace-theme-cobalt"]
                                :requires ["ace"]}
                               {:file "libs/ace/theme-crimson_editor.js"
                                :provides ["ace-theme-crimson-editor"]
                                :requires ["ace"]}
                               {:file "libs/ace/theme-dawn.js"
                                :provides ["ace-theme-dawn"]
                                :requires ["ace"]}
                               {:file "libs/ace/theme-github.js"
                                :provides ["ace-theme-github"]
                                :requires ["ace"]}
                               {:file "libs/ace/theme-twilight.js"
                                :provides ["ace-theme-twilight"]
                                :requires ["ace"]}
                               
                               {:file "libs/ace/mode-clojure.js"
                                :provides ["ace-mode-clojure"]
                                :requires ["ace"]}
                               {:file "libs/ace/mode-csound.js"
                                :provides ["ace-mode-csound"]
                                :requires ["ace"]}
                               {:file "libs/ace/ext-searchbox.js"
                                :provides ["ace-ext-searchbox"]
                                :requires ["ace"]}]
                :asset-path "js/out"
                :output-to "resources/public/js/main.js"
                :output-dir "resources/public/js/out"
                :source-map true
                :verbose true}}]})

(def sass-config
  {:executable-path "sass"
   :input-dir "resources/scss"
   :output-dir "resources/public/css"})

(defrecord Figwheel []
  component/Lifecycle
  (start [config]
    (ra/start-figwheel! config)
    config)
  (stop [config]
    (ra/stop-figwheel!)
    config))

(defrecord SassWatcher [executable-path input-dir output-dir]
  component/Lifecycle
  (start [config]
    (if (not (:sass-watcher-process config))
      (do
        (println "Figwheel: Starting SASS watch process")
        (assoc config :sass-watcher-process
               (.exec (Runtime/getRuntime)
                      (str executable-path " --watch " input-dir ":" output-dir))))
      config))
  (stop [config]
    (when-let [process (:sass-watcher-process config)]
      (println "Figwheel: Stopping SASS watch process")
      (.destroy process))
    config))

(def system
  (atom
   (component/system-map
    :figwheel (map->Figwheel figwheel-config)
    :sass (map->SassWatcher sass-config))))

(defn start []
  (swap! system component/start))

(defn stop []
  (swap! system component/stop))

(defn reload []
  (stop)
  (start))

(defn repl []
  (ra/cljs-repl))

;; Start the components and the repl
(start)
(repl)


