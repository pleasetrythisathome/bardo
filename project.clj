(defproject bardo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :jvm-opts ^:replace ["-Xms512m" "-Xmx512m" "-server"]
  :source-paths  ["src"]

  :cljsbuild {:builds [{:id "bardo"
                        :source-paths ["src"]
                        :compiler {:output-to "bardo.js"
                                   :output-dir "out"
                                   :optimizations :none
                                   :source-map true}]})
