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

(defn on-interval
  "runs a function intended to produce side effects at a target speed while the function returns truthy"
  ([step] (on-interval step {}))
  ([step {:keys [target tolerance step]
          :or {target 16
               tolerance 1
               step 0.5}}]
     (go-loop [last-time (t/now)
               wait target]

              (let [time (now)]

                (when (step time)

                  ;; converge wait time on 60fps and recur
                  (let [last (- time last-time)
                        new (if (< (- target tolerance)
                                   last
                                   (+ target tolerance))
                              wait
                              ((if (< target last) - +) wait step))]

                    (when (< 0 wait)
                      (<! (timeout wait)))

                    (recur time
                           new)))))))

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
    (on-interval step {:target 16})))

(defn transition
  ([state target] (transition state target {}))
  ([state target {:keys [duration easing]
                  :or {duration 500
                       easing :cubic-in-out}}]
     (let [out (chan (sliding-buffer 1))
           interpolator (interpolate state target)
           ease-fn (ease easing)
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
;; (log-transition (transition [1 10] [5 3] {:duration 2000}))
;; (log-transition (transition {:a 1 :b 10} {:a 5 :b 300} {:duration 1000 :easing :elastic-in-out}))
