{:cljx {}
 :dev {:clean-targets ["out" :target-path]
       :hooks [leiningen.cljsbuild]

       :resources-paths ["dev-resources"]
       :source-paths ["dev"]

       :dependencies [[om "0.4.1"]
                      [com.cemerick/piggieback "0.1.3"]
                      [criterium "0.4.3"]
                      [org.clojure/tools.namespace "0.2.4"]
                      [weasel "0.4.0-SNAPSHOT"]]

       :plugins [[com.cemerick/clojurescript.test "0.3.0"]
                 [lein-cljsbuild "1.0.3"]
                 [lein-pdo "0.1.1"]]

       :cljsbuild
       {:builds [{:id "test"
                  :source-paths ["target/src/cljs" "target/test/cljs"]
                  :compiler {:optimizations :none
                             :pretty-print true
                             :source-map true
                             :output-dir "dev-resources/public/out/"
                             :output-to "dev-resources/public/js/test.js"}}]}}}
