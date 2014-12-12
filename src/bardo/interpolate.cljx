(ns bardo.interpolate
  #+cljs (:require-macros [cljs.core.match.macros :refer [match]])
  (:require [clojure.set :refer [union]]
            #+clj [clojure.core.match :refer [match]]
            #+cljs [cljs.core.match]
            [bardo.ease :as ease]))

;; a protocol for birthing new values from nil
(defprotocol IFresh
  (fresh [x]))

(extend-protocol IFresh

  #+clj java.lang.Number
  #+cljs number
  (fresh [x]
    0)

  #+clj clojure.lang.Sequential
  #+cljs List
  (fresh [x]
    '())

  #+clj clojure.lang.PersistentArrayMap
  #+cljs PersistentArrayMap
  (fresh [x]
    {}))

(def hash-map? (every-pred coll? (complement sequential?)))

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

(defn wrap-infinite [x y]
  ;;(println :wrap-infinite [x y])
  (if (every? sequential? [x y])
    (match (mapv counted? [x y])
           [false false] (throw
                        (#+cljs js/Exception #+clj #+cljs js/Exception #+clj Exception. "Cannot interpolate between two uncounted sequences"))
           [false _] [(take (count y) x) y]
           [_ false] [x (take (count x) y)]
           [_ _] [x y])
    [x y]))

(defn juxt-args [& fns]
  (fn [& args]
    (map-indexed (fn [idx f]
                   (f (nth args idx nil)))
                 fns)))

(defn symmetrical-error
  "calls (f x y) (f y x) and returns [x y] where f is a function (f x y) that returns [x y]"
  [s msg f]
  (when (or (apply f s)
            (apply f (reverse s)))
    (throw
     (#+cljs js/Exception #+clj Exception. msg))))

(defn pair-pred [pred]
  (comp (partial every? identity)
        (juxt-args pred (complement pred))))

(defn wrap-errors
  "throw appropriate errors if you can't interpolate between two values"
  [x y]
  (let [types {"seq" sequential?
               "hash-map" hash-map?
               "number" number?}]
    (doseq [[type pred] types]
      (symmetrical-error [x y]
                         (str "Cannot interpolate between a " type " and something else")
                         (pair-pred pred)))
    [x y]))

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
                 [0 (_ :guard sequential?)] (vec (take (count start) v))
                 [1 (_ :guard sequential?)] (vec (take (count end) v))
                 [_ _] v))))))

(declare interpolate)

(defprotocol IInterpolate (-interpolate [start end]))

(extend-protocol IInterpolate

  #+clj java.lang.Number
  #+cljs number
  (-interpolate [start end]
    (fn [t]
      (+ start (* t (- end start)))))

  #+clj clojure.lang.Sequential
  #+cljs List
  (-interpolate [start end]
    (fn [t]
      (into [] (for [k (range (Math/max (count start)
                                        (count end)))]
                 (->> [(nth start k nil) (nth end k nil)]
                      (apply wrap-nil)
                      (apply interpolate)
                      (#(% t)))))))

  #+clj clojure.lang.IPersistentMap
  #+cljs PersistentArrayMap
  (-interpolate [start end]
    (fn [t]
      (seq (for [k (->> [start end]
                        (map keys)
                        (map set)
                        (apply union))]
             [k (->> [start end]
                     (map k)
                     (apply interpolate)
                     (#(% t)))])))))

(defn interpolate [start end]
  (let [wrapped (some->> [start end]
                         (apply wrap-nil)
                         (apply wrap-errors)
                         (apply wrap-infinite))]
    (let [can-interpolate (mapv #(satisfies? IInterpolate %) wrapped)]
      (if (apply = true can-interpolate)
        ((apply wrap-size wrapped) (apply -interpolate wrapped))
        (do
          (throw
           (#+cljs js/Exception #+clj Exception. (str "Cannot interpolate between " start " and " end))))))))

(defn mix
  [start end]
  (fn [t]
    ((interpolate (start t) (end t)) t)))

(defn blend
  [intrpl end]
  (fn [t]
    ((interpolate (intrpl t) end) t)))

(defn chain
  ([intrpl end] (chain intrpl end 0.5))
  ([intrpl end mid]
     (let [start (ease/shift intrpl 0 mid)
           end (-> (intrpl 1)
                   (interpolate end)
                   (ease/shift mid 1))]
       (fn [t]
         (cond
          (< t mid) (start t)
          (>= t mid) (end t))))))

(defn make-range [coll]
  (->> coll
       ((juxt (comp #(conj % 0)
                    (partial drop-last 1))
              identity))
       (apply interleave)
       (partition 2)
       (into [])))

(defn shift-parts
  [f input output]
  (assert (= (count input) (count output)) "ranges must be the same length")
  (let [[input output] (->> [input output]
                            (mapv (comp vec (partial map-indexed vector) make-range)))]
    (fn [t]
      (cond
       (= t 0) (f 0)
       (= t 1) (f 1)
       :else (let [[idx [istart iend]] (first (filter (comp (fn [[start end]] (<= start t end)) second) input))
                   [_ [estart eend]] (get output (int idx))]
               ((ease/shift f istart iend estart eend) t))))))


(defn pipeline
  [start second & states]
  (let [n (+ 2 (count states))
        input (map (partial * (/ 1 (dec n))) (range 1 n))
        pow-2 (reverse (take (dec n) (iterate #(/ % 2) 1)))]
    (-> (reduce chain (interpolate start second) states)
        (shift-parts input pow-2))))

