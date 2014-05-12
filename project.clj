(defproject bardo "0.1.0-SNAPSHOT"
  :description "A clojure(script) library to assist with transitions between dimensions"
  :url "https://github.com/pleasetrythisathome/bardo"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :source-paths ["src/cljx"]
  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [com.andrewmcveigh/cljs-time "0.1.1"]]

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}

                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}]}

  :hooks [cljx.hooks]

  :profiles {:dev {:clean-targets ["out" :target-path]
                   :hooks [leiningen.cljsbuild]
                   :resources-paths ["dev-resources"]
                   :source-paths ["dev-resources/tools/http" "dev-resources/tools/repl"]
                   :dependencies [[om "0.4.1"]
                                  [com.facebook/react "0.8.0.1"]
                                  [ring "1.2.1"]
                                  [compojure "1.1.6"]
                                  [enlive "1.1.5"]]
                   :plugins [[com.keminglabs/cljx "0.3.2"]
                             [com.cemerick/clojurescript.test "0.3.0"]
                             [com.cemerick/austin "0.1.3"]
                             [lein-cljsbuild "1.0.1"]]

                   :cljsbuild
                   {:builds {:bardo
                             {:source-paths ["dev-resources/tools/repl"]
                              :compiler
                              {:optimizations :whitespace
                               :pretty-print true}}}}

                   :injections [(require '[ring.server :as http :refer [run]]
                                         'cemerick.austin.repls)
                                (defn browser-repl-env []
                                  (reset! cemerick.austin.repls/browser-repl-env
                                          (cemerick.austin/repl-env)))
                                (defn browser-repl []
                                  (cemerick.austin.repls/cljs-repl
                                   (browser-repl-env)))]}})
