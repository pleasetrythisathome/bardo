(ns bardo.ease
  (:require [clojure.string :as str]))

(defn interpolator
  "canonical definition of a higher order interpolator function"
  [f]
  (fn [t]
    (f t)))

(defn wrap
  "useful for wrapping easers in other easers, especially in theading macros"
  [f ease]
  (interpolator (comp f ease)))

(defn clamp
  "clamp input to function so that (<= 0 t 1)"
  [f]
  (fn [t]
    (f (cond
         (< t 0) 0
         (> t 1) 1
         :else t))))

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

(defn partition-range
  "for a range partition into pairs of each number and it's following
   ex. [0 0.25 0.5 0.75 1] => [[0 0.25] [0.25 0.5] [0.5 0.75] [0.75 1]]"
  [coll]
  (->> (interleave coll (rest coll))
       (partition 2)
       ;; needed?
       (mapv vec)))

(defn shift-parts
  "shift input t over many steps
   ex. (shift-part f [0 0.5 1] [0 0.3 1]) expands to roughly
   =>  (fn [t]
         (cond
           (<= 0 t 0.5) (shift f 0 0.5 0 0.3)
           (<= 0.5 t 1) (shift f 0.5 1 0.3 1)))"
  [f input output]
  (assert (= (count input) (count output)) "ranges must be the same length")
  (let [[input output] (->> [input output]
                            (mapv (comp vec (partial map-indexed vector) partition-range)))]
    (fn [t]
      (cond
        (= t 0) (f 0)
        (= t 1) (f 1)
        :else (let [[idx [istart iend]] (first (filter (comp (fn [[start end]] (<= start t end)) second) input))
                    [_ [estart eend]] (get output (int idx))]
                ((shift f istart iend estart eend) t))))))

(defn flip
  "reverse"
  [f]
  (fn [t]
    (- 1 (f (- 1 t)))))

(defn reflect
  "symmetrical around t = 0.5"
  [f]
  (fn [t]
    (* 0.5 (if (< t 0.5)
             (f (* 2 t))
             (- 2 (f (- 2 (* 2 t))))))))

(def modes {:in     identity
            :out    flip
            :in-out reflect
            :out-in (comp reflect flip)})

;; adapted from https://github.com/warrenm/AHEasing and
;; and https://github.com/mbostock/d3/blob/master/src/interpolate/ease.js

(def PI #?(:clj  Math/PI
           :cljs (.-PI js/Math)))
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
  "raise t to power e"
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
    (Math/pow 2 (* 10 (dec t)))))

(defn elastic
  "Modeled after the damped sine wave y = sin(13PI_2*x)*pow(2, 10 * (x - 1))"
  [t]
  (* (Math/sin (* 13 PI_2 t))
     (Math/pow 2 (* 10 (dec t)))))

(defn back
  "Modeled after the overshooting cubic y = x^3-x*sin(x*pi)"
  [t]
  (- (* t t t)
     (* t (Math/sin (* t PI)))))

(defn bounce
  "Modeled after some fun bouncing stuff"
  [t]
  (cond
    (< t (/ 1 2.75)) (* 7.5625 t t)
    (< t (/ 2 2.75)) (+ (* 7.5625
                           (- t (/ 1.5 2.75))
                           (- t (/ 1.5 2.75)))
                        0.75)
    (< t (/ 2.5 2.75)) (+ (* 7.5625
                             (- t (/ 2.5 2.75))
                             (- t (/ 2.5 2.75)))
                          0.9375)
    :else (+ (* 7.5625
                (- t (/ 2.625 2.75))
                (- t (/ 2.625 2.75)))
             0.984375)))

(def ease-fns {:linear  (constantly identity)
               :quad    (constantly quad)
               :cubic   (constantly cubic)
               :poly    poly
               :sine    (constantly sine)
               :circle  (constantly circle)
               :exp     (constantly exp)
               :elastic (constantly elastic)
               :back    (constantly back)
               :bounce  (constantly bounce)})

(defn ease
  "easing function constructor. takes key-mode where mode #{:in :out :in-out}
  ex. :bounce-in-out will return a symetrical bounce easing curve"
  [key & args]
  (let [[fn start end] (str/split (name key) #"-")
        ease-fn (or (get ease-fns (keyword fn))
                    (:linear ease-fns))
        mode (or (->> [start end]
                      (filter identity)
                      (str/join "-")
                      keyword
                      (get modes))
                 (:in modes))]
    ((comp clamp mode) (apply ease-fn args))))
