(ns bardo.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [cljs.core.async
             :refer [put! take! <! >! chan timeout sliding-buffer]
             :as async]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]))

(enable-console-print!)

(defn log
  "logs cljs stuff as js stuff for inspection"
  [& args]
  (.apply (.-log js/console) js/console (clj->js (map clj->js args))))

(defn time-since
  [time]
  (let [start (c/to-long time)
        now (c/to-long (t/now))]
    (- now start)))

(def ease-fns {:linear identity})

(defn tween
  [state target]
  (fn [t]
    ))

(defn transition
  ([state target] (transition state target 500))
  ([state target duration] (transition state target duration :linear))
  ([state target duration ease]
     (let [start (t/now)
           ease-fn (get ease-fns ease)]
       (go-loop [t 0]
                (let [since (time-since start)]
                  (log t)

                  ;; timeout should scale to approach 60fps
                  ;; (<! (timeout 10))

                  (when (< since duration)
                    (recur (ease-fn (/ since duration)))))))))

(transition {} {})
