(defproject bardo "0.1.0"
  :description "A clojure(script) library to assist with transitions between dimensions"
  :url "https://github.com/pleasetrythisathome/bardo"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :resource-paths ["target/src/cljs"]

  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-2342"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [clj-time "0.8.0"]
                 [com.andrewmcveigh/cljs-time "0.1.1"]]
  )
