(ns bardo.examples.tutorial
  (:require [bardo.ease :as ease]
            [bardo.interpolate :as interpolate]
            [bardo.transition :as transition]))

;; an interpolator is a function (fn [t]) where (<= 0 t 1) returns an intermediate value

;; a simple interpolator between two numbers
(defn intrpl-nums [a b]
  (fn [t]
    (+ a (* t (- b a)))))

(def zero->ten (intrpl-nums 0 10))
(zero->ten 0.5)
;; => 5.0

;; interpolators are intended to be used either to produce a sequence of values,
;; or to produce an intermediate value at a point in time

;; we can produce sequences of values from an interpolator using normal clojure functions

(mapv zero->ten [0 0.5 1])
;; => [0 5.0 10]

;; we can produce lazy sequences using bardo.interpolate/into-lazy-seq

;; say we want an infinite sequence of values approaching the start of the interpolator
;; only 100 are computed
(take 100 (interpolate/into-lazy-seq zero->ten (iterate #(/ % 2) 1)))

;; ========== Easing ==========

;; Bardo defines an easing function is a single-arity function ```(fn [t] (f t))``` where f produces a new value t. Easing functions are most commonly used to provide different curves to time values, but can be used to produce a varity of effects.

(defn slower [t]
  (/ t 2))
(slower 0.5)
;; => 0.25

;; we can also write this as a higher order function
(defn easer [f]
  (fn [t]
    (f t)))
(def slower (easer #(/ % 2)))
(slower 0.5)
;; => 0.25

;; other easiers can provide useful boundaries
(defn clamp
  "clamp input to function so that (<= 0 t 1)"
  [f]
  (fn [t]
    (f (cond
        (< t 0) 0
        (> t 1) 1
        :else t))))

(def not-too-fast (easer (clamp #(/ % 2))))
(not-too-fast 1)
;; => 1/2
(not-too-fast 1.5)
;; => 1/2

;; or we could shift the domain of an input

(defn shift
  "shifts the domain of input from [cmin cmax] to [nmin nmax]"
  ([f cmin cmax] (shift f cmin cmax 0 1))
  ([f cmin cmax nmin nmax]
     (fn [t]
       (f (-> t
              (- cmin)
              (/ (- cmax cmin))
              (* (- nmax nmin))
              (+ nmin))))))

(def percent (shift slower 0 100))
(percent 50)
;; => 1/4
