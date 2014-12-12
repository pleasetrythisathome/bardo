(load-file "./build.util.clj")
(require '[build.util :as build])

(set-env!
 :name 'bardo
 :version "0.1.0-SNAPSHOT"
 :description "A clojure(script) library to assist with transitions between dimensions"
 :url "http://github.com/pleasetrythisathome/bardo"
 :dependencies (build/deps)
 :src-paths    #{"src"}
 :rsc-paths    #{"resources"})

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[deraen.boot-cljx      :refer [cljx]]
 '[pandeiro.http         :refer [serve]]
 '[clojure.tools.namespace.repl :refer [set-refresh-dirs]]

 '[boot.util            :as util]
 '[boot.pod             :as pod])

(deftask growl
  "Growl notifications during build."
  []
  (let []
    (pod/add-dependencies-worker {:dependencies '[[jakemcc/clojure-gntp "0.1.1"]]})
    (fn [continue]
      (fn [event]
        (try
          (util/with-let [ret (continue event)]
            (pod/call-worker
             (if (zero? @*warnings*)
               `(jakemcc.clojure-gntp.gntp/message "Boot - success" ""
                                                   :appname "boot"
                                                   :app "boot"
                                                   :notification "boot-notify")
               `(jakemcc.clojure-gntp.gntp/message "Boot - warning(s)" ~(deref *warnings*)
                                                   :appname "boot"
                                                   :app "boot"
                                                   :notification "boot-notify"))))
          (catch Throwable t
            (pod/call-worker
             `(jakemcc.clojure-gntp.gntp/message "Boot - error" ~(str (.getMessage t))
                                                 :appname "boot"
                                                 :app "boot"
                                                 :notification "boot-notify"))
            (throw t)))))))

(deftask development
  "watch and compile cljx, css, cljs, init cljs-repl and push changes to browser"
  []
  (let [src (:src-paths (get-env))]
    (set-env! :src-paths (conj src "dev")))
  (apply set-refresh-dirs (get-env :src-paths))
  (comp (serve :dir "target")
        (watch)
        ;; (speak :success "")
        (growl)
        (cljx)
        (cljs-repl)
        (cljs :output-to "main.js"
              :optimizations :none
              :unified true
              :source-map true
              :pretty-print true)
        (reload :port 3449)))

(defn dev
  []
  (require 'dev)
  (in-ns 'dev))
