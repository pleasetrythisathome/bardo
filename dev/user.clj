(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]

            [weasel.repl.websocket :as weasel]
            [cemerick.piggieback :as piggieback]

            ;; [bardo.core :refer :all]
            ))

(defn browser-repl-env []
  (weasel/repl-env :ip "0.0.0.0" :port 9001))

(defn start-cljs-repl! []
  (piggieback/cljs-repl :repl-env (browser-repl-env)))

(comment
  (stop)
  (start))
