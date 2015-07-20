(defproject bardo "0.1.0"
  :description "A clojure(script) library to assist with transitions between dimensions"
  :url "https://github.com/pleasetrythisathome/bardo"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :source-paths ["target/src/clj" "target/test/clj"]
  :resource-paths ["target/src/cljs"]

  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-2342"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [clj-time "0.8.0"]
                 [com.andrewmcveigh/cljs-time "0.1.1"]]

  :cljx {:builds [{:source-paths ["src"],
                   :output-path "target/src/clj",
                   :rules :clj}
                  {:source-paths ["src"],
                   :output-path "target/src/cljs",
                   :rules :cljs}
                  {:source-paths ["test"],
                   :output-path "target/test/clj",
                   :rules :clj}
                  {:source-paths ["test"],
                   :output-path "target/test/cljs",
                   :rules :cljs}]}
  :plugins [[com.keminglabs/cljx "0.3.2"]]
  :hooks [cljx.hooks])
