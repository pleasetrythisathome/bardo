(ns bardo.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [cljs.core.async
             :refer [put! take! <! >! chan timeout sliding-buffer close! alts!]
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

(defn now []
  (if-let [now (.-now (.-performance js/window))]
    (.call now (.-performance js/window))
    (.now js/Date)))

(defn on-raf
  [step]
  (if-let [native (let [vendors ["" "ms" "moz" "webkit" "o"]]
                    (->> vendors
                         (map #(aget js/window (str % "requestAnimationFrame")))
                         (filter identity)
                         (first)))]
    (let [frame (chan)]
      (go-loop []
               (.call native js/window (fn [time]
                                         (put! frame time)))
               (let [time (<! frame)]
                 (when (step time)
                   (recur)))))
    (let [speed-target 16
          speed-tolerance 1
          speed-step 0.5]

      (go-loop [last-time (t/now)
                wait speed-target]

               (let [time (now)]

                 (when (step time)

                   ;; converge wait time on 60fps and recur
                   (let [speed-last (- time last-time)
                         speed-new (if (< (- speed-target speed-tolerance)
                                          speed-last
                                          (+ speed-target speed-tolerance))
                                     wait
                                     ((if (< speed-target speed-last) - +) wait speed-step))]

                     (when (< 0 wait)
                       (<! (timeout wait)))

                     (recur time
                            speed-new))))))))

(defn transition
  ([state target] (transition state target 500))
  ([state target duration] (transition state target duration :cubic-in-out))
  ([state target duration easing & ease-args]
     (let [out (chan (sliding-buffer 1))
           interpolator (interpolate state target)
           ease-fn (ease easing ease-args)
           start (now)]

       (on-raf (fn [time]
                 (let [since (- time start)
                       done? (> since duration)
                       t (if done?
                           1
                           (ease-fn (/ since duration)))
                       step (interpolator (ease-fn t))]
                   (put! out step)
                   (when done?
                     (close! out))
                   (not done?))))
       out)))

;; examples

#_(defn log-transition [t]
    (go-loop []
             (when-let [v (<! t)]
               (log v)
               (recur))))

;; (log-transition (transition 1 5))
;; (log-transition (transition [1 10] [5 3] 2000))
;; (log-transition (transition {:a 1 :b 10} {:a 5 :b 300} 1000 :elastic-in-out))
