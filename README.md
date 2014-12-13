# bardo

A clojure(script) library to assist with transitions between dimensions. Bardo defines semantics for represting interpolators between data and provides a suite of tools for manipulating them. 

More eastern themed names! Really? But I have a good reason for this one, I promise! From [wikipedia](http://en.wikipedia.org/wiki/Bardo),

```
The Tibetan word bardo (བར་དོ་ Wylie: bar do) means literally "intermediate state"
—also translated as "transitional state" or "in-between state" or "liminal state".
```

Generally, the term "bardo" represents the inbetween state between life and death, during which one's consciousness is not connected to the outside world. 

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

You can produce lazy sequences using bardo.interpolate/into-lazy-seq

```clj
;; produce infinite sequence of values approaching the start of the interpolator
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

## License

Copyright © 2014 Dylan Butman

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
