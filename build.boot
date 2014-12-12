(set-env!
 :name 'bardo
 :version "0.1.0-SNAPSHOT"
 :description "A clojure(script) library to assist with transitions between dimensions"
 :url "http://github.com/pleasetrythisathome/bardo"
 :dependencies (concat (->>
                        '[[adzerk/boot-cljs "0.0-2371-27"]
                         [adzerk/boot-cljs-repl "0.1.6"]
                         [adzerk/boot-reload "0.1.8"]
                         [deraen/boot-cljx "0.1.0"]
                         [pandeiro/boot-http "0.2.0"]
                         [com.cemerick/double-check "0.6.1"]
                         [om "0.8.0-beta2"]
                         [prismatic/om-tools "0.3.6" :exclusions [org.clojure/clojure]]
                         [sablono "0.2.22" :exclusions [com.facebook/react]]
                         [shodan "0.4.1"]
                         [clojure-complete "0.2.4"]
                         [org.clojure/tools.namespace "0.2.7"]]
                        (mapv #(conj % :scope "test")))
                       '[[org.clojure/clojure "1.7.0-alpha4"]
                         [org.clojure/clojurescript "0.0-2411"]
                         [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                         [org.clojure/core.match "0.2.2"]
                         [clj-time "0.8.0"]
                         [com.andrewmcveigh/cljs-time "0.2.4"]])
 :src-paths    #{"src"}
 :rsc-paths    #{"resources"})

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[deraen.boot-cljx      :refer [cljx]]
 '[pandeiro.http         :refer [serve]]
 '[clojure.tools.namespace.repl :refer [set-refresh-dirs]])

(deftask development
  "watch and compile cljx, css, cljs, init cljs-repl and push changes to browser"
  []
  (let [src (:src-paths (get-env))]
    (set-env! :src-paths (conj src "dev")))
  (apply set-refresh-dirs (get-env :src-paths))
  (comp (serve :dir "target")
        (watch)
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
