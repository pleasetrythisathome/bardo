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

;; we can produce lazy sequences using bardo.interpolate/into-lazy-seq

;; say we want an infinite sequence of values approaching the start of the interpolator
;; only 100 are computed
(take 100 (interpolate/into-lazy-seq zero->ten (iterate #(/ % 2) )))

;; ========== Easing ==========

;; an
