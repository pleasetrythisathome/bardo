(set-env!
 :name 'bardo
 :version "0.1.0-SNAPSHOT"
 :description "A clojure(script) library to assist with transitions between dimensions"
 :url "http://github.com/pleasetrythisathome/bardo"
 :dependencies (vec
                (concat (->>
                         '[[adzerk/bootlaces "0.1.11"]
                           [adzerk/boot-cljs "0.0-3308-0"]
                           [adzerk/boot-cljs-repl "0.1.7"]
                           [adzerk/boot-reload "0.2.0"]
                           [deraen/boot-cljx "0.2.0"]
                           [pandeiro/boot-http "0.3.0"]
                           [com.cemerick/double-check "0.6.1"]
                           [org.clojure/tools.namespace "0.2.7"]
                           [clojure-complete "0.2.4"]
                           [garden "1.2.5"]
                           [om "0.8.0-beta3"]
                           [sablono "0.2.22" :exclusions [com.facebook/react]]]
                         (mapv #(conj % :scope "test")))
                        '[[org.clojure/clojure "1.7.0"]
                          [org.clojure/clojurescript "0.0-3308"]
                          [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                          [org.clojure/core.match "0.3.0-alpha4"]
                          [clj-time "0.10.0"]
                          [com.andrewmcveigh/cljs-time "0.3.10"]]))
 :source-paths  #{"src"}
 :resource-paths #{"resources" "src"})

(require
 '[adzerk.bootlaces      :refer :all]
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[deraen.boot-cljx      :refer [cljx]]
 '[pandeiro.http         :refer [serve]]
 '[clojure.tools.namespace.repl :refer [set-refresh-dirs]])


(def +version+ "0.1.2-SNAPSHOT")
(bootlaces! +version+)

(task-options!
 pom {:project 'bardo
      :version +version+
      :description "A clojure(script) library to assist with transitions between dimensions"
      :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}
      :url "https://github.com/pleasetrythisathome/bardo"
      :scm {:url "https://github.com/pleasetrythisathome/bardo"}})

(deftask development
  "watch and compile cljx, css, cljs, init cljs-repl and push changes to browser"
  []
  (set-env! :source-paths #(conj % "dev" "examples"))
  (apply set-refresh-dirs (get-env :source-paths))
  (comp (serve :dir "target")
        (watch)
        (cljx)
        (cljs-repl)
        (cljs :output-to "main.js"
              :optimizations :none
              ;; :unified true
              :source-map true
              :pretty-print true)
        (reload :port 3449)))

(defn dev
  []
  (require 'dev)
  (in-ns 'dev))
