(ns bardo.core-test
  #+cljs
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [bardo.core :as bardo]
            #+clj [clojure.test :refer :all]
            #+cljs [weasel.repl :as repl]
            #+cljs [cljs.core.async
                    :refer [put! take! <! >! chan timeout sliding-buffer close! alts!]
                    :as async]))

#+cljs
(enable-console-print!)

#+cljs
(when-not (repl/alive?)
  (repl/connect  "ws://localhost:9001" :verbose true))

#+cljs
(defn log
  "logs cljs stuff as js stuff for inspection"
  [& args]
  (.apply (.-log js/console) js/console (clj->js (map clj->js args))))

#+cljs
(defn log-transition [t]
  (go (loop []
        (when-let [v (<! t)]
          (log v)
          (recur)))))

;; (log-transition (bardo/transition 1 5))

;; (log-transition (transition [1 10] [5 3] {:duration 2000}))
;; (log-transition (transition {:a 1 :b 10} {:a 5 :b 300} {:duration 1000 :easing :elastic-in-out}))
