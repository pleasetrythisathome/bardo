(ns bardo.transition
  #?(:cljs
     (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [bardo.ease :refer [ease]]
            [bardo.interpolate :refer [interpolate]]
            #?@(:clj  [[clj-time.core :as t]
                       [clj-time.coerce :as c]
                       [clojure.core.async
                        :refer [put! take! <! >! <!! >!! chan timeout sliding-buffer close! go go-loop alts!]
                        :as async]]
                :cljs [[cljs.core.async
                        :refer [put! take! <! >! chan timeout sliding-buffer close! alts!]
                        :as async]
                       [cljs-time.core :as t]
                       [cljs-time.coerce :as c]])))


(defn now []
  #?(:clj  (c/to-long (t/now))
     :cljs (if-let [now (.-now (.-performance js/window))]
             (.call now (.-performance js/window))
             (.now js/Date))))

#?(:clj
   (defn set-interval
     "runs a function intended to produce side effects at a target speed while the function returns truthy"
     ([step] (set-interval step {}))
     ([step {:keys [target tolerance step]
             :or   {target    16
                    tolerance 1
                    step      0.5}}]
      (go-loop [last-time (now)
                wait target]

               (let [time (now)]

                 (when (step time)

                   ;; converge wait time on target and recur
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

   :cljs
   (defn set-interval
     [step-fn]
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
                    (when (step-fn time)
                      (recur))))))))

(defn transition
  ([state target] (transition state target {}))
  ([state target {:keys [duration easing]
                  :or   {duration 500
                         easing   :cubic-in-out}}]
   (let [out (chan (sliding-buffer 1))
         interpolator (interpolate state target)
         ease-fn (ease easing)
         start (now)]

     (set-interval
       (fn [time]
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
