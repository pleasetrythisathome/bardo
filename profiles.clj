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

       :plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                 [com.cemerick/clojurescript.test "0.3.0"]
                 [lein-cljsbuild "1.0.3"]
                 [lein-pdo "0.1.1"]]

        :repl-options {:nrepl-middleware [cider.nrepl.middleware.classpath/wrap-classpath
                                          cider.nrepl.middleware.complete/wrap-complete
                                          cider.nrepl.middleware.info/wrap-info
                                          cider.nrepl.middleware.inspect/wrap-inspect
                                          cider.nrepl.middleware.macroexpand/wrap-macroexpand
                                          cider.nrepl.middleware.stacktrace/wrap-stacktrace
                                          cider.nrepl.middleware.test/wrap-test
                                          cider.nrepl.middleware.trace/wrap-trace
                                          cider.nrepl.middleware.undef/wrap-undef
                                          cemerick.piggieback/wrap-cljs-repl
                                          cljx.repl-middleware/wrap-cljx]}
       :cljsbuild
       {:builds [{:id "test"
                  :source-paths ["target/src/cljs" "target/test/cljs"]
                  :compiler {:optimizations :none
                             :pretty-print true
                             :source-map true
                             :output-dir "dev-resources/public/out/"
                             :output-to "dev-resources/public/js/test.js"}}]}}}
