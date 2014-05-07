(ns bardo.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [cljs.core.async
             :refer [put! take! <! >! chan timeout sliding-buffer close!]
             :as async]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [bardo.ease :refer [ease]]
            [bardo.interpolate :refer [interpolate]]))

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

(defn transition
  ([state target] (transition state target 500))
  ([state target duration] (transition state target duration :cubic-in-out))
  ([state target duration easing & ease-args]
     (let [out (chan (sliding-buffer 1))
           interpolator (interpolate state target)
           ease-fn (ease easing ease-args)
           start (t/now)
           speed-target 16
           speed-tolerance 1
           speed-step 0.5]

       (go-loop [last-time start
                 wait speed-target]

                (let [since (time-since start)
                      done? (> since duration)
                      t (if done?
                          1
                          (ease-fn (/ since duration)))
                      step (interpolator (ease-fn t))]

                  (put! out step)

                  (if done?
                    (close! out)

                    ;; converge wait time on 60fps and recur
                    (let [speed-last (- since last-time)
                          speed-new (if (< (- speed-target speed-tolerance)
                                           speed-last
                                           (+ speed-target speed-tolerance))
                                      wait
                                      ((if (< speed-target speed-last) - +) wait speed-step))]

                      (when (< 0 wait)
                        (<! (timeout wait)))

                      (recur since
                             speed-new)))))
       out)))

;; (transition {} {})
