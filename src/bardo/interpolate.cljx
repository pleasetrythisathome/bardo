(ns bardo.interpolate
  (:require [clojure.set :refer [union]]
            [clojure.core.match :refer [match]]))

;; a protocol for birthing new values from nil
(defprotocol IFresh
  (fresh [x]))

(extend-protocol IFresh

  #+clj java.lang.Number
  #+cljs number
  (fresh [x]
    0)

  #+clj clojure.lang.Sequential
  #+cljs Sequential
  (fresh [x]
    '())

  #+clj clojure.lang.PersistentArrayMap
  #+cljs PersistentArrayMap
  (fresh [x]
    {}))

(defn wrap-nil
  "if a value is nil, replace it with a fresh value of the other
  value if it satisfies IFresh"
  [start end]
  (match [start end]
         [nil nil] nil
         [nil end] (if (satisfies? IFresh end)
                     [(fresh end) end]
                     [nil end])
         [start nil] (if (satisfies? IFresh start)
                       [start (fresh start)]
                       [start nil])
         [start end] [start end]))

(defn is-lazy? [s]
  (= clojure.lang.LazySeq (class s)))

(defn prevent-infinite [x y]
  (match (mapv is-lazy? [x y])
         [true true] (throw
                      (Exception. "Cannot interpolate between two LazySeq"))
         [true _] [(take (count y) x) y]
         [_ true] [x (take (count x) y)]
         [_ _] [x y]))

(defn coerce
  "attempt to coerce values to the same type"
  [x y]
  (let [classes (mapv class [x y])]
    (if (apply = classes)
      (prevent-infinite x y)
      (match [x y]
             ;; [seq seq]
             [([& _] :seq) ([& _] :seq)] (prevent-infinite x y)
             ;; [vector seq]
             [[& _] ([& _] :seq)] (coerce [(seq x) y])
             ;; [seq vector]
             [([& _] :seq) [& _]] (coerce [x (seq y)])
             [_ _] [nil nil]))))

(def hash-map? (every-pred coll? (complement sequential?)))

(defn wrap-size
  "removed keys not present in start or end of interpolation"
  [start end]
  (let []
    (fn [intrpl]
      (fn [t]
        (let [v (intrpl t)]
          (match [t v]
                 [0 (_ :guard hash-map?)] (select-keys v (keys start))
                 [1 (_ :guard hash-map?)] (select-keys v (keys end))
                 [0 (_ :guard sequential?)] (take (count start) v)
                 [1 (_ :guard sequential?)] (take (count end) v)
                 [_ _] v))))))

(defprotocol IInterpolate (-interpolate [start end]))

(defn interpolate [start end]
  (let [coerced (some->> [start end]
                         (apply wrap-nil)
                         (apply coerce))]
    (let [can-interpolate (->> coerced
                               (mapv (partial satisfies? IInterpolate)))]
      (if (apply = true can-interpolate)
        ((apply wrap-size coerced) (apply -interpolate coerced))
        (do
          (throw
           (Exception. (str "Cannot interpolate between " start " and " end))))))))

(extend-protocol IInterpolate

  #+clj java.lang.Number
  #+cljs number
  (-interpolate [start end]
    (fn [t]
      (+ start (* t (- end start)))))

  #+clj clojure.lang.Sequential
  #+cljs Sequential
  (-interpolate [start end]
    (fn [t]
      (seq (for [k (range (Math/max (count start)
                                    (count end)))]
             (->> [(nth start k nil) (nth end k nil)]
                  (apply wrap-nil)
                  (apply interpolate)
                  (#(% t)))))))

  #+clj clojure.lang.IPersistentMap
  #+cljs IPersistentMap
  (-interpolate [start end]
    (fn [t]
      (into {} (for [k (->> [start end]
                            (map keys)
                            (map set)
                            (apply union))]
                 [k (->> [start end]
                         (map k)
                         (apply interpolate)
                         (#(% t)))])))))


(comment
  (defn intrpl [start end]
    (let [i (interpolate start end)]
      (mapv #(i %) [0 0.5 1])))

  (mapv #(satisfies? IInterpolate %) [1 ""])
  (satisfies? IInterpolate "")
  (intrpl 1 2)
  (intrpl [1 2] [5 6])
  (intrpl [1 2] (repeat 5))

  (intrpl [1 2] (into [] (range 5)))
  ;; size not wrapped correctly.
  ;; coerce is taking from them since they are lazy
  ;; maybe this is correct behavior?
  (intrpl (range 5) [1 2])

  ;; fails correctly
  (intrpl [1 5] [2 [1 2]])
  (intrpl (repeat 5) (repeat 2))

  (intrpl {:a 0 :c 1} {:a 5 :b 2})
  )
