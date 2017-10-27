(ns clj-rdate.holiday-cal
  (:require [clj-time.core :as t]
            [clojure.string :as s]
            [clj-rdate.internal.fn :as intfn]))

; This is not the long term plan for this, but works for the moment
(defmulti get-calendar (fn [cal-name]
  (if (s/includes? cal-name "|") :multi-part cal-name)))
(defmulti is-holiday? (fn [cal dt] (:type cal)))
(defn is-not-holiday? [cal dt] (not (is-holiday? cal dt)))
(defmulti holidays (fn [cal from-dt to-dt] (:type cal)))
(defmulti rule-holidays (fn [rule from-dt to-dt] (:type rule)))

(defmethod get-calendar :multi-part [cal-name]
  {:type ::multi :cals (map #(get-calendar %1) (s/split cal-name "|"))})

(defmethod is-holiday? ::multi [cal dt]
  (reduce #(or %2 (is-holiday? %1) false (:cals cal))))

(defmethod is-holiday? ::weekends [cal dt]
  (some #(t/day-of-week dt) (:weekend-days cal)))

(defmethod is-holiday? ::rule-based [cal dt]
  (let [century-- (quot (t/year dt) 100)
        holidays (holidays cal century--)]
    (contains? holidays (intfn/to-local-date dt))))

(defmethod get-calendar "Weekdays" [_]
  {:type ::weekends :weekend-days [6 7]})
; 
; (defmethod get-calendar "GBP" [_]
;   {:type ::multi :cals [
;     (get-calendar "Weekdays")
;     {:type ::rule-based :rules [
;       {:type ::periodic-rule :name "New Years Day" :period "1y" :rule "1JAN+0d@Weekdays"}
;       ]}]})
