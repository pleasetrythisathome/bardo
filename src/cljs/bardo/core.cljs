(ns bardo.core
  (:require [cljs.core.async
             :refer :all
             :exclude [map reduce into partition partition-by take merge]
             :as async]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]))

(enable-console-print!)

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
                  (println t)

                  ;; timeout should scale to approach 60fps
                  (<! (timeout 10))

                  (when (< since duration)
                    (recur (ease-fn (/ since duration)))))))))

(transition {} {})
