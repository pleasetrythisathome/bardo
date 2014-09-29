(ns bardo.core-test
  #+cljs
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [bardo.core :as bardo]
            [bardo.ease :as ease]
            [bardo.interpolate :as intrpl]
            #+clj [clojure.pprint :refer [pprint]]
            #+clj [clojure.core.async
                   :refer [put! take! <! >! <!! >!!  chan timeout close! go go-loop]
                   :as async]
            #+clj [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            #+clj [clojure.test :refer :all]
            #+cljs [weasel.repl :as repl]
            #+cljs [cljs.core.async
                    :refer [put! take! <! >! chan timeout close!]
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

#+clj
(def log-chan (chan))

#+clj
(defn log
  [& args]
  (put! log-chan args))

#+clj
(go-loop []
  (when-let [args (<! log-chan)]
    (pprint args)
    (recur)))

(defn log-transition [t]
  (go (loop []
        (when-let [v (<! t)]
          (log v)
          (recur)))))

(comment
  (defn test-intrpl [start end]
    (let [i (interpl/interpolate start end)]
      (mapv #(i %) [0 0.5 1])))

  (test-intrpl 1 2)
  (test-intrpl [1 2] [5 6])
  (test-intrpl [1 2] (repeat 5))

  ;; size wrapped
  (test-intrpl [1 2] (into [] (range 5)))
  ;; wrap-infinite is taking 2 since they are not counted
  ;; this is correct behavior
  (test-intrpl (range 5) [1 2])

  ;; fails correctly
  (test-intrpl [1 5] [2 [1 2]])
  (test-intrpl (repeat 5) (repeat 2))

  (test-intrpl {:a 0 :c 1} {:a 5 :b 2})

  (test-intrpl {:a 1} {:a 10 :b 2})
  (test-intrpl nil 2 0.5)
  (test-intrpl 2 nil 0.5)

  (log-transition (bardo/transition 1 5))
  (log-transition (bardo/transition [1 10] [5 3] {:duration 2000}))
  (log-transition (bardo/transition {:a 1 :b 10} {:a 5 :b 300} {:duration 1000 :easing :elastic-in-out}))

  )
