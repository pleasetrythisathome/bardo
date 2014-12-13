# bardo

A clojure(script) library to assist with transitions between dimensions. Bardo defines semantics for represting interpolators between data and provides a suite of tools for manipulating them. 

More eastern themed names! Really? But I have a good reason for this one, I promise! From wikipedia,

```
The Tibetan word bardo (བར་དོ་ Wylie: bar do) means literally "intermediate state"—also translated as "transitional state" or "in-between state" or "liminal state".
```

Generally, the term "bardo" represents the inbetween state between life and death, during which one's consciousness is not connected to the outside world. 

## What is an interpolator?

Bardo defines an interpolator as a higher order function that returns a single-arity function ```(fn [t])``` where ```t``` is a number ```(<= 0 t 1)``` that produces a corresponding intermediate value.



## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
