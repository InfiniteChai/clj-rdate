(ns clj-rdate.internal.holiday-cal-impl
  (:require [clj-time.core :as t]
            [clojure.string :as s]
            [clj-rdate.internal.fn :as intfn]))

(refer 'clj-rdate.core)
; This is not the long term plan for this, but works for the moment.
; Long term we need a way to extend to any calendar methodology we desire

(defmethod get-calendar :multi-part [cal-name]
  {:type :clj-rdate.core/multi :cals (map #(get-calendar %1) (s/split cal-name "|"))})
(defmethod get-calendar "Weekdays" [_]
  {:type :clj-rdate.core/weekends :weekend-days [6 7]})

(defmethod is-holiday? :clj-rdate.core/multi [cal dt]
  (reduce #(or %2 (is-holiday? %1) false (:cals cal))))

(defmethod is-holiday? :clj-rdate.core/weekends [cal dt]
  (some #{(t/day-of-week dt)} (:weekend-days cal)))
