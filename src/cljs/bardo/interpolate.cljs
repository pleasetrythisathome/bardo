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
      (mapv (comp #(% t) interpolate) this target))))

(print ((interpolate [1 2] [5 6]) 0.5))
