(ns clj-rdate.internal.fn
  "Internal functions for use only within the clj-rdate library"
  (:require [clj-time.core :as t]))

(defn easter-sunday [date-constructor dt year-increment]
  (let [year (+ (t/year dt) year-increment)
        a (rem  year 19)
        b (quot year 100)
        c (rem  year 100)
        d (quot b 4)
        e (rem  b 4)
        f (quot (+ b 8) 25)
        g (quot (+ b (- f) 1) 3)
        h (rem  (+ (* 19 a) b (- d) (- g) 15) 30)
        i (quot c 4)
        k (rem  c 4)
        l (rem  (+ 32 (* 2 e) (* 2 i) (- h) (- k)) 7)
        m (quot (+ a (* 11 h) (* 22 l)) 451)
        n (quot (+ h l (- (* 7 m)) 114) 31)
        p (rem  (+ h l (- (* 7 m)) 114) 31)]
    (if (< year 1583)
      (throw (IllegalArgumentException. "Easter sunday only supported from 1583"))
      (date-constructor dt year n (inc p)))))

(defmulti to-local-date class)
(defmethod to-local-date org.joda.time.LocalDate [dt] dt)
(defmethod to-local-date :default [dt] (t/local-date (t/year dt) (t/month dt) (t/day dt)))
