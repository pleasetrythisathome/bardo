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
           ease-fn (get ease-fns ease)
           speed-target 16
           speed-tolerance 1
           speed-step 0.5]
       (go-loop [t 0
                 last-time start
                 wait speed-target]
                (let [since (time-since start)]
                  (log t)

                  (when (< since duration)

                    ;; converge wait time on 60fps
                    (let [speed-last (- since last-time)
                          speed-new (if (< (- speed-target speed-tolerance)
                                           speed-last
                                           (+ speed-target speed-tolerance))
                                      wait
                                      ((if (< speed-target speed-last) - +) wait speed-step))]

                      (when (< 0 wait)
                        (<! (timeout wait)))

                      (recur (ease-fn (/ since duration)) since speed-new))))))))

;; (transition {} {})
