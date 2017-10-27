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
  {:type :clj-rdate.core/weekends :weekend-days {6 nil 7 nil}})

(defmethod get-calendar "GBP" [_]
  {:type :clj-rdate.core/multi :cals [
    (get-calendar "Weekdays")
    {:type :clj-rdate.core/rule-based :rules [
      ; Examples of the periodic form for standard holidays
      {:type ::rule-periodic :name "New Years Day" :period "1y" :rule "1JAN+0b"}
      {:type ::rule-periodic :name "Good Friday" :period "1y" :rule "0E-2d"}
      {:type ::rule-periodic :name "Easter Monday" :period "1y" :rule "0E+1d"}
      {:type ::rule-periodic :name "May Bank Holiday" :period "1y" :rule "1MAY+1st MON"}
      {:type ::rule-periodic :name "Spring Bank Holiday" :period "1y" :rule "1MAY+Last MON"}
      {:type ::rule-periodic :name "Summer Bank Holiday" :period "1y" :rule "1AUG+Last MON"}
      {:type ::rule-periodic :name "Christmas Day" :period "1y" :rule "25DEC+0b"}
      {:type ::rule-periodic :name "Boxing Day" :period "1y" :rule "26DEC+0b"}
      ; Examples of specific date holidays which may be added (or removed)
      {:type ::rule-specific-date :name "Queen's Diamond Jubilee" :day 05 :month 06 :year 2012}
      {:type ::rule-specific-date :name "2012 May Bank Holiday" :day 04 :month 06 :year 2012}
      {:type ::rule-specific-date-removal :name "2012 May Bank Holiday Moved" :day 28 :month 05 :year 2012}
      ]}]})

(defmethod is-holiday? :clj-rdate.core/multi [cal dt]
  (reduce #(or %1 (is-holiday? %2 dt)) false (:cals cal)))

(defmethod is-holiday? :clj-rdate.core/weekends [cal dt]
  (contains? (:weekend-days cal) (t/day-of-week dt)))

  (defn- holidays-in-period [cal from-year years]
    (let [from-dt (t/local-date from-year 1 1)
          to-dt (t/local-date (+ from-year (dec years)) 12 31)]
      (holidays cal from-dt to-dt)))
  (def ^{:private true} cached-holidays-in-period (memoize holidays-in-period))

(defmethod is-holiday? :clj-rdate.core/rule-based [cal dt]
  (let [period 100
        val (quot (t/year dt) period)
        hols (cached-holidays-in-period cal (* period val) period)]
    (contains? hols dt)))

(defmulti rule-holidays (fn [rule from-dt to-dt] (:type rule)))
(defmethod rule-holidays ::rule-periodic [rule from-dt to-dt]
  (into (sorted-map)
    (map #(vector (rdate-add (:rule rule) %1) (:name rule))
      (rdate-range from-dt to-dt (:period rule)))))
(defmethod rule-holidays ::rule-specific-date [rule from-dt to-dt]
  (let [dt (t/local-date (:year rule) (:month rule) (:day rule))]
    (if (and (not (t/after? from-dt dt)) (not (t/before? to-dt dt))) {dt (:name rule)} {})))

(defmethod rule-holidays ::rule-specific-date-removal [rule from-dt to-dt]
  (let [dt (t/local-date (:year rule) (:month rule) (:day rule))]
    (if (and (not (t/after? from-dt dt)) (not (t/before? to-dt dt))) {dt nil} {})))

(defmethod holidays :clj-rdate.core/rule-based [cal from-dt to-dt]
  (into {} (filter #(not (nil? (second %))) (into (sorted-map) (map #(rule-holidays %1 from-dt to-dt) (:rules cal))))))
