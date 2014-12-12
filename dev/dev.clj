(ns dev
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]

            [bardo.transition :refer :all]
            [bardo.ease :as ease]
            [bardo.interpolate :refer :all]))

(defn reset []
  (refresh))
