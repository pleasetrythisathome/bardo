(ns bardo.interpolate-test
  (:require [bardo.interpolate :refer :all]
            [bardo.ease :as ease]
            ;; [clojure.test.check.core :as sc]
            ;; [clojure.test.check.generators :as gen]
            ;; [clojure.test.check.properties :as prop :include-macros true]
            ))

(comment
  (defn bench [intrpl steps]
    (time
     (for [t (range 0 1 (/ 1 steps))]
       (intrpl t))))


  (-> (pipeline 1 10 50 100)
      ;;(apply [2/3])
      (bench 60)
      )

  (-> (interpolate [1 2] [3 4])
      (ease/clamp)
      (ease/shift 0.5 1)
      (apply [0.5]))

  (-> (interpolate [1 2] [3 4])
      (ease/wrap (ease/ease :cubic-in-out))
      (mix (interpolate [1 2] [5 6]))
      ;;(apply [0.1])
      (bench 60)
      (->> (mapv (partial mapv float))))

  (-> (interpolate {:a 1 :b 2} {:a 3 :b 5})
      (blend {:a 9 :b 10})
      ;;(apply [0.7])
      (bench 600))

  (-> (interpolate {:a 1 :b 2} {:a 3 :b 5})
      (bench 6))

  (-> (interpolate 0 5)
      (chain 100)
      ;;(apply [0.5])
      (bench 60))
  )
