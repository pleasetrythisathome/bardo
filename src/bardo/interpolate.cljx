(ns bardo.interpolate
  (:require [clojure.set :refer [union]]))

;; a protocol for birthing new values from nil
(defprotocol IBirth (birth [x]))

(extend-protocol IBirth

    #+clj java.lang.Number
    #+cljs number
    (birth [x]
      0))

(defprotocol IInterpolate (interpolate [start end]))

(extend-protocol IInterpolate

  nil
  (interpolate [start end]
    (interpolate (birth end) end))

  #+clj java.lang.Number
  #+cljs number
  (interpolate [start end]
    (cond
       (nil? end) (interpolate start (birth start))
       :else (fn [t]
               (+ start (* t (- end start))))))

  #+clj clojure.lang.PersistentVector
  #+cljs PersistentVector
  (interpolate [start end]
    (fn [t]
      (mapv (comp #(% t) interpolate) start end)))

  #+clj clojure.lang.PersistentList
  #+cljs List
  (interpolate [start end]
    (fn [t]
      (map (comp #(% t) interpolate) start end)))

  #+clj clojure.lang.PersistentArrayMap
  #+cljs PersistentArrayMap
  (interpolate [start end]
    (fn [t]
      (into {} (for [k (->> [start end]
                            (map keys)
                            (map set)
                            (apply union))]
                 [k (apply (comp #(% t) interpolate) (map k [start end]))])))))
