# bardo

A clojure(script) library to assist with transitions between dimensions. Bardo defines semantics for represting interpolators between data and provides a suite of tools for manipulating them. 

More eastern themed names! Really? But I have a good reason for this one, I promise! From [wikipedia](http://en.wikipedia.org/wiki/Bardo),

```
The Tibetan word bardo (བར་དོ་ Wylie: bar do) means literally "intermediate state"
—also translated as "transitional state" or "in-between state" or "liminal state".
```

Generally, the term "bardo" represents the inbetween state between life and death, during which one's consciousness is not connected to the outside world. 

## Installation

Bardo is available in [clojars](https://clojars.org/bardo). Add this to your ```:dependencies``` vector.

```clj
[bardo "0.1.0"]
```

```clj
;; import vars used in feature examples below
(ns bardo.features
  (require [bardo.ease :refer [wrap ease shift clamp]]
           [bardo.interpolate :refer [interpolate into-lazy-seq mix blend chain pipeline]]
           [bardo.transition :refer [transition]]
```

## Features

Interpolate between values

```clj
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

```

Compose interpolators

```clj
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

```

Combine with easing functions

```clj
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

```

Output values over time

```clj
(transition 0 5 {:duration 1000 :easing :cubic-in-out})
;; => channel onto which values are placed that closes 1000ms later.
;; resolution is 60 values/second by default
```

## What is an interpolator?

Bardo defines an interpolator as a higher order function that returns a single-arity function ```(fn [t])``` where ```t``` is a number ```(<= 0 t 1)``` that produces a corresponding intermediate value.

an interpolator between two numbers ```a``` and ```b``` can be defined as
```clj
(defn intrpl-nums [a b]
  (fn [t]
    (+ a (* t (- b a)))))
    
(def zero->ten (intrpl-nums 0 10))
(zero->ten 0.5)
;; => 5.0
```

interpolators can be used to produce a sequence of values, or to produce an intermediate value at a point in time. We can produce sequences of values from an interpolator using normal clojure functions.

```clj
(mapv zero->ten [0 0.5 1])
;; => [0 5.0 10]
```

You can produce lazy sequences using ```bardo.interpolate/into-lazy-seq```

```clj
(take 100 (interpolate/into-lazy-seq zero->ten (iterate #(/ % 2) 1)))
;; only 100 are computed
```

## Easing

Bardo defines an easing function is a single-arity function ```(fn [t] (f t))``` where f produces a new value t. Easing functions are most commonly used to provide different curves to time values, but can be used to produce a varity of effects.

```clj
(defn faster [t]
  (+ 0.1 t))
(faster 0)
;; => 0.1

;; we can also write this as a higher order function
(defn easer [f]
  (fn [t]
    (f t)))
(def faster (easer (partial + 0.1)))
(faster 0)
;; => 0.1
       
```

Easing functions can also be used to define or change input boundaries. ```clamp``` and ```shift``` can be found in ```bardo.ease```

```clj
(defn clamp
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
```

Bardo provides a higher level api for creating common easing curve functions ```bardo.ease/ease``` all of the [standard easing functions](http://easings.net/) are provided in skewer case (cubicInOut -> :cubic-in-out

```clj
(def cubic (ease/ease :cubic-in-out))
(mapv cubic (range 0 1 (/ 1 10)))
;; => [0.0 0.004 0.032 0.108 0.256 0.5 0.744 0.892 0.968 0.996]
```

## Interpolation Protocols

Bardo can automatically create interpolation functions from data. Bardo supports sequences, hashmaps, and numbers out of the box, but can be extended to support any clojure value. Interpolateable types satisfy:

```clj
;; perform interpolation
(defprotocol IInterpolate
  (-interpolate [start end]))

;; return "fresh" value in case of nil or nonexistent
(defprotocol IFresh
  (fresh [x]))
```

```bardo.interpolate/interpolate``` provides an entry point that wraps nil values, checks for type compatibility, and wraps differently shaped data. ```interpolate``` should be used instead of ```-interpolate``` unless you want to bypass these wrapping mechanisms.

Here's an example of extending bardo to interpolate between garden colors. 

```clj
(ns garden
  (:require [bardo.interpolate :refer [IFresh IInterpolate -interpolate interpolate]]
            [garden.color :as color]))

(extend-protocol IFresh
  garden.color.CSSColor
  (fresh [s]
    (color/rgb 255 255 255)))

(extend-protocol IInterpolate
  garden.color.CSSColor
  (-interpolate [start end]
    (let [[start end] (map color/as-rgb [start end])]
      (fn [t]
        (color/color+ start (color/color* t (color/color- end start)))))))

(-> (interpolate (color/hsl 10 50 50) (color/rgb 0 255 0))
    (map [0 0.25 0.5 0.75 1])
    clojure.pprint/pprint)
;; =>
;; ({:alpha nil,
;;   :lightness nil,
;;   :saturation nil,
;;   :hue nil,
;;   :blue 64,
;;   :green 85,
;;   :red 191}
;;  {:red 191.0,
;;   :green 127.5,
;;   :blue 64.0,
;;   :hue nil,
;;   :saturation nil,
;;   :lightness nil,
;;   :alpha nil}
;;  {:red 191.0,
;;   :green 170.0,
;;   :blue 64.0,
;;   :hue nil,
;;   :saturation nil,
;;   :lightness nil,
;;   :alpha nil}
;;  {:red 191.0,
;;   :green 212.5,
;;   :blue 64.0,
;;   :hue nil,
;;   :saturation nil,
;;   :lightness nil,
;;   :alpha nil}
;;  {:alpha nil,
;;   :lightness nil,
;;   :saturation nil,
;;   :hue nil,
;;   :blue 64,
;;   :green 255,
;;   :red 191})
```                  

## Examples and Development

I've been using the [boot](https://github.com/boot-clj/boot) build tool, it's great. To build bardo from source, install boot, and run ```boot development``` to get a full development setup complete with file server, cljx, cljs-repl, and js reloading. 

## Graphics Integration

Bardo is well suited for integration into graphical context like [Om](https://github.com/swannodette/om) (or [Reagent](http://holmsand.github.io/reagent/), [Quil](https://github.com/quil/quil), [libGDX](https://github.com/oakes/play-clj), etc. etc.). 

A simple Om example using the transition helper.
```clj
(defn mover
  [{:keys [x y]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:x x
       :y y})
    om/IWillReceiveProps
    (will-receive-props [_ {:keys [x y]}]
      (let [[px py] ((juxt :x :y) (om/get-props owner))
            intrpl-ch (transition {:x px :y py} {:x x :y y} {:duration 1000})]
        (go-loop []
          (when-let [{:keys [x y]} (<! intrpl-ch)]
            (om/set-state! owner :x x)
            (om/set-state! owner :y y)
            (recur)))))
    om/IRenderState
    (render-state [_ {:keys [x y]}]
      (html
       [:div
        {:style {:position "fixed"
                 :top y
                 :left x}}
        [:div
         "Location"]
        [:div
         (str "x: " (int x))]
        [:div
         (str "y: " (int y))]]))))
```

Mixes between interpolators that represent overlapping time domains can be mixed by shifting the domains of the interpolators. 

```clj
(def a 0)
(def b 10)
(def intrpl (interpolate a b))
(map intrpl [0 0.5 1])
;; => (0 5.0 10)
;; => (fn [t]) : t [0 -> 1] : a -> b

;; interrupt at dt and interpolate to c
(def dt 0.6)
(def c 50)
(def dintrpl (interpolate (intrpl dt) c))
(map dintrpl [0 0.5 1])
;; => (6.0 28.0 50.0)
;; (fn [t]) : t [0 -> 1] : (intrpl dt) -> c

;; to mix, we need to shift the domain of the input to intrpl
(def sintrpl (ease/shift intrpl 0 (- 1 dt) dt 1))
(map sintrpl [0 (- 1 dt) 1])
;; => (6.0 10.0 16.0)
;; we get a higher output at 1 because we've extended the domain
;; (fn [t]) ; t [0 -> (- 1 dt)] : (intrpl dt) -> b
(-> identity
    (ease/shift 0 (- 1 dt) dt 1)
    (map [0 (- 1 dt) 1]))
;; => (0.6 1.0 1.6)

(def mixed (mix sintrpl dintrpl))
(map mixed [0 0.25 0.5 0.75 1])
;; => (6.0 10.625 19.5 32.625 50.0)
```

A full om example is [here](https://github.com/pleasetrythisathome/bardo/blob/master/examples/om.cljs). More examples coming soon...

## Disclaimer

Bardo is very much alpha software. It's been used in production in some form, but is still under active development. The API is subject to change. Thoughts, comments, feature, and pull requests welcome.

## License

Copyright © 2014 Dylan Butman

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
