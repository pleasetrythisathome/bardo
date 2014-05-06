(ns bardo.ease
  (:refer-clojure :exclude [reverse])
  (:require [clojure.string :refer [split join]]))

(defn clamp
  [f]
  (fn [t]
    (if (< t 0)
      0
      (if (< 1 t)
        1
        (f t)))))

(defn reverse
  [f]
  (fn [t]
    (- 1 (f (- 1 t)))))

(defn reflect
  [f]
  (fn [t]
    (* 0.5 (if (< t 0.5)
             (f (* 2 t))
             (- 2 (f (- 2 (* 2 t))))))))

(def modes {:in identity
            :out reverse
            :in-out reflect
            :out-in (comp reflect reverse)})

;; Adapted from https://github.com/warrenm/AHEasing and
;; and https://github.com/mbostock/d3/blob/master/src/interpolate/ease.js

(def PI   (.-PI js/Math))
(def PI_2 (/ (.-PI js/Math) 2))

(defn quad
  "Modeled after the parabola y = x^2"
  [t]
  (* t t))

(defn cubic
  "Modeled after the cubic y = x^3"
  [t]
  (* t t t))

(defn poly
  [e]
  (fn [t]
    (.pow js/Math t e)))

(defn sine
  "Modeled after quarter-cycle of sine wave"
  [t]
  (inc (.sin js/Math (* (dec t) PI_2))))

(defn circle
  "Modeled after shifted quadrant IV of unit circle"
  [t]
  (- 1 (.sqrt js/Math (- 1 (* t t )))))

(defn exp
  "Modeled after the exponential function y = 2^(10(x - 1))"
  [t]
  (if (= t 0)
    t
    (.pow js/Math 2 (* 10 (dec t)))))

(defn elastic
  "Modeled after the damped sine wave y = sin(13PI_2*x)*pow(2, 10 * (x - 1))"
  [t]
  (* (.sin js/Math (* 13 PI_2 t))
     (.pow js/Math 2 (* 10 (dec t)))))

(defn back
  "Modeled after the overshooting cubic y = x^3-x*sin(x*pi)"
  [t ]
  (- (* t t t)
     (* t (.sin js/Math (* t PI)))))

(defn bounce
  "Modeled after some fun bouncing stuff"
  [t]
  (cond
    (< t (/ 1 2.75))    (* 7.5625 t t)
    (< t (/ 2 2.75))    (+ (* 7.5625
                              (- t (/ 1.5 2.75))
                              (- t (/ 1.5 2.75)))
                           0.75)
    (< t (/ 2.5 2.75))  (+ (* 7.5625
                              (- t (/ 2.5 2.75))
                              (- t (/ 2.5 2.75)))
                           0.9375)
    :else               (+ (* 7.5625
                              (- t (/ 2.625 2.75))
                              (- t (/ 2.625 2.75)))
                           0.984375)))

(def ease-fns {:linear identity
               :quad quad
               :cubic cubic
               :poly poly
               :sine sine
               :circle circle
               :exp exp
               :elastic elastic
               :back back
               :bounce bounce})

(defn ease
  [key & args]
  (let [[fn start end] (map keyword (split (name key) "-"))
        ease-fn (or (get ease-fns fn)
                    identity)
        mode (or (get modes (join "-" [start end]))
                 identity)]
    (comp clamp node (apply ease-fn args))))
