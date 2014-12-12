(ns bardo.ease
  (:refer-clojure :exclude [reverse])
  (:require [clojure.string :as str]))

(defn wrap
  [f ease]
  (fn [t]
    (f (ease t))))

(defn clamp
  [f]
  (fn [t]
    (f (cond
        (< t 0) 0
        (> t 1) 1
        :else t))))

(defn shift
  ([f cmin cmax] (shift f cmin cmax 0 1))
  ([f cmin cmax nmin nmax]
     (fn [t]
       (f (-> t
              (- cmin)
              (/ (- cmax cmin))
              (* (- nmax nmin))
              (+ nmin))))))

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
               ((shift f istart iend estart eend) t))))))

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

;; translated from https://github.com/warrenm/AHEasing and
;; and https://github.com/mbostock/d3/blob/master/src/interpolate/ease.js

(def PI   #+clj Math/PI #+cljs (.-PI js/Math))
(def PI_2 (/ PI 2))


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
    (Math/pow t e)))

(defn sine
  "Modeled after quarter-cycle of sine wave"
  [t]
  (inc (Math/sin (* (dec t) PI_2))))

(defn circle
  "Modeled after shifted quadrant IV of unit circle"
  [t]
  (- 1 (Math/sqrt (- 1 (* t t)))))

(defn exp
  "Modeled after the exponential function y = 2^(10(x - 1))"
  [t]
  (if (= t 0)
    t
    (exp 2 (* 10 (dec t)))))

(defn elastic
  "Modeled after the damped sine wave y = sin(13PI_2*x)*pow(2, 10 * (x - 1))"
  [t]
  (* (Math/sin (* 13 PI_2 t))
     (Math/pow 2 (* 10 (dec t)))))

(defn back
  "Modeled after the overshooting cubic y = x^3-x*sin(x*pi)"
  [t ]
  (- (* t t t)
     (* t (Math/sin (* t PI)))))

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

(def ease-fns {:linear (constantly identity)
               :quad (constantly quad)
               :cubic (constantly cubic)
               :poly poly
               :sine (constantly sine)
               :circle (constantly circle)
               :exp (constantly exp)
               :elastic (constantly elastic)
               :back (constantly back)
               :bounce (constantly bounce)})

(defn ease
  [key & args]
  (let [[fn start end] (str/split (name key) #"-")
        ease-fn (or (get ease-fns (keyword fn))
                    identity)
        mode (or (->> [start end]
                      (filter identity)
                      (str/join "-")
                      keyword
                      (get modes))
                 (:in modes))]
    ((comp clamp mode) (apply ease-fn args))))
