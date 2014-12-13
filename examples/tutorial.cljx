(ns bardo.examples.tutorial
  (:require [bardo.ease :refer :all]
            [bardo.interpolate :refer :all]
            [bardo.transition :refer :all]))

;; ========== Interpolators ==========

(def times [0 0.5 1])

;; primatives
(map (interpolate 0 10) times)
;; => (0 5.0 10)

;; sequences
(map (interpolate [0 1] [5 9]) times)
;; => ([0 1] [2.5 5.0] [5 9])

(map (interpolate {:a 0 :b 1} {:a 5 :b 0}) times)
;; => ({:a 0, :b 1} {:b 0.5, :a 2.5} {:a 5, :b 0})

;; different dimensions
(map (interpolate [0] [5 9]) times)
;; => ([0] [2.5 4.5] [5 9])
(map (interpolate [0 nil] [5 9]) times)
;; => ([0 0] [2.5 4.5] [5 9])

(map (interpolate {:b 1} {:a 5}) times)
;; => ({:b 1} {:b 0.5, :a 2.5} {:a 5})

;; coerce sequence types
(map (interpolate [1 2] (repeat 5)) times)
;; => ([1 2] [3.0 3.5] [5 5])

(map (interpolate (repeat 5) (repeat 5)) times)
;; => java.lang.Exception: Cannot interpolate between two uncounted sequences

;; throw errors if you can't interpolate
(map (interpolate [1 2] {:a 5 :b 0}) times)
;; => java.lang.Exception: Cannot interpolate between a seq and something else

;; make lazy sequences if you want them
(take 10 (into-lazy-seq (interpolate 0 10) (range 0 1 (/ 1 10000))))

;; ========== Compose ==========

(def times [0 0.25 0.5 0.75 1])

;; interpolate between outputs
(-> (interpolate 0 5)
    (mix (interpolate 0 10))
    (map times))
;; => (0 1.5625 3.75 6.5625 10)

;; blend to new target
(-> (interpolate 0 5)
    (blend 10)
    (map times))
;; => (0 3.4375 6.25 8.4375 10)

;; chaining
(-> (interpolate 0 5)
    (chain 20)
    (map times))
;; => (0.0 2.5 5.0 12.5 20.0)

;; set midpoint
(-> (interpolate 0 5)
    (chain 20 0.8)
    (map times))
;; => (0.0 1.5625 3.125 4.6875 20.0)

;; pipeline
(-> (pipeline [0 10 50 3000])
    (map times))
;; => (0.0 7.5 30.0 787.4999999999993 3000.0)

;; set input steps
(-> (pipeline [0 10 50 3000] [0 0.1 0.9 1])
    (map times))
;; => (0.0 17.5 30.0 42.5 3000.0)

;; ========== Easing ==========

(def times (concat (range 0 1 (/ 1 20)) [1]))
(defn sig [n]
  (fn [x]
    (-> x
        (* (Math/pow 10 n))
        Math/round
        (/ (Math/pow 10 n)))))

(-> (interpolate 0 5)
    (wrap (ease :cubic-in-out))
    (map times)
    (->> (map (sig 2))))
;; => (0.0 0.0 0.02 0.07 0.16 0.31 0.54 0.86 1.28 1.82 2.5 3.18 3.72 4.14 4.46 4.69 4.84 4.93 4.98 5.0 5.0)

(-> (interpolate 0 5)
    (clamp)
    (shift 0.5 1)
    (map times)
    (->> (map (sig 2))))
;; => (0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.5 1.0 1.5 2.0 2.5 3.0 3.5 4.0 4.5 5.0)

;; ========== Transition ==========

(transition 0 5 {:duration 1000 :easing :cubic-in-out})
;; => channel onto which values are placed that closes 1000ms later.
;; resolution is 60 values/second by default
