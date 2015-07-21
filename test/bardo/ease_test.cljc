(ns bardo.ease-test
  (:require [clojure.test :refer :all]
            [bardo.ease :refer :all]))


(deftest ease-test
  (is (= 6 ((interpolator inc) 5)))
  (is (= 7 ((wrap inc inc) 5)))
  (testing "clamp"
    (is (= 1.5 ((clamp inc) 0.5)))
    (is (= 2 ((clamp inc) 10e4)))
    (is (= 1 ((clamp inc) -10e4)))
    (is (= 2 ((clamp inc) 1))))
  (testing "shift"
    (is (= 0 ((shift identity 5 10) 5)))
    (is (= 1 ((shift identity 5 10) 10)))
    (is (= 2 ((shift inc 5 10) 10)))
    (is (= 5 ((shift identity 5 10 5 10) 5)))
    (is (= 10 ((shift identity 5 10 5 10) 10))))
  (testing "partition range"
    (is (= [[0 0.25] [0.25 0.5] [0.5 0.75] [0.75 1]]
           (partition-range [0 0.25 0.5 0.75 1])))
    (is (= [[1 3] [3 8] [8 11]]
           (partition-range [1 3 8 11]))))
  (testing "shift parts"
    (is (= 0.2 ((shift-parts identity [0.1 1] [0.2 1]) 0.1)))
    (is (= 1 ((shift-parts identity [0 0.5 1] [0 0.3 1]) 1)))
    (is (= 0 ((shift-parts identity [0 0.5 1] [0 0.3 1]) 0)))
    (is (thrown? java.lang.AssertionError
                 ((shift-parts identity [0] [0 0.1]) 0))))
  (testing "flip"
    (is (zero? ((flip (partial + 5)) 5)))
    (is (= -5 ((flip (partial + 5)) 0)))
    (is (= -3 ((flip (partial + 5)) 2)))
    (is (= 10 ((flip identity) 10)) "=> (- 1 (- 1 10))"))
  (testing "reflect"
    (is (= 10.0 ((reflect identity) 10)) "=> (* 10 2 0.5)")
    (is (= 0.0 ((reflect identity) 0)))
    (is (= 0.5 ((reflect inc) 0)))))



