(ns bardo.interpolate)

(defprotocol IInterpolate (interpolate [this target]))

(extend-protocol IInterpolate
  number
  (interpolate [this target]
    (fn [t]
      (+ this (* t (- target this)))))
  PersistentVector
  (interpolate [this target]
    (fn [t]
      (mapv (comp #(% t) interpolate) this target)))
  List
  (interpolate [this target]
    (fn [t]
      (map (comp #(% t) interpolate) this target)))
  PersistentArrayMap
  (interpolate [this target]
    (fn [t]
      (reduce-kv (fn [ret k v]
                   (let [prev (or (get ret k) v)]
                     (merge ret (hash-map k ((interpolate prev v) t)) ))) this target))))
