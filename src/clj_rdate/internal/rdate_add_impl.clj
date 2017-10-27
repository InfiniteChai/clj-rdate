(ns clj-rdate.internal.rdate-add-impl
  "Implementation details for rd/rdate-add"
  (:require [clj-time.core :as t]
            [clj-rdate.internal.fn :as intfn]))
(refer 'clj-rdate.core)

(defmethod rdate-add [:clj-rdate.core/date-obj :clj-rdate.core/rdate] [dt rd] (rdate-add rd dt))
(defmethod rdate-add [:clj-rdate.core/rdate :clj-rdate.core/rdate] [left right]
  {:type :clj-rdate.core/compound :parts [left right]})
(defmethod rdate-add [:clj-rdate.core/string-obj :clj-rdate.core/date-obj] [rd dt] (rdate-add (rdate rd) dt))
(defmethod rdate-add [:clj-rdate.core/date-obj :clj-rdate.core/string-obj] [dt rd] (rdate-add (rdate rd) dt))
(defmethod rdate-add [:clj-rdate.core/string-obj :clj-rdate.core/string-obj] [l r] (rdate-add (rdate l) (rdate r)))
(defmethod rdate-add [:clj-rdate.core/days :clj-rdate.core/date-obj] [rd dt]
  (t/plus dt (t/days (:period rd))))
(defmethod rdate-add [:clj-rdate.core/weeks :clj-rdate.core/date-obj] [rd dt]
  (t/plus dt (t/weeks (:period rd))))
(defmethod rdate-add [:clj-rdate.core/months :clj-rdate.core/date-obj] [rd dt]
  (t/plus dt (t/months (:period rd))))
(defmethod rdate-add [:clj-rdate.core/years :clj-rdate.core/date-obj] [rd dt]
  (t/plus dt (t/years (:period rd))))
(defmethod rdate-add [:clj-rdate.core/weekdays :clj-rdate.core/date-obj] [rd dt]
  (let [weekday-diff (- (t/day-of-week dt) (:weekday rd))
        period-chg (cond (and (< (:period rd) 0) (> weekday-diff 0)) 1 (and (> (:period rd) 0) (< weekday-diff 0)) -1 :else 0)
        period (+ (:period rd) period-chg)]
      (t/plus dt (t/days (- (* period 7) weekday-diff)))))
(defmethod rdate-add [:clj-rdate.core/nth-weekdays :clj-rdate.core/date-obj] [rd dt]
  "Get the nth weekday in the given month. Exception if out of bounds"
  (let [wkd (t/day-of-week dt)
        wkd-1st (inc (mod (- (dec wkd) (dec (mod (t/day dt) 7))) 7))
        wkd-1st-diff (- wkd-1st (:weekday rd))
        period (if (> wkd-1st-diff 0) (:period rd) (dec (:period rd)))
        days (inc (- (* 7 period) wkd-1st-diff))]
    (date-constructor dt (t/year dt) (t/month dt) days)))
(defmethod rdate-add [:clj-rdate.core/nth-last-weekdays :clj-rdate.core/date-obj] [rd dt]
  "Get the nth last weekday in the given month. Exception if out of bounds"
  (let [ldom (t/last-day-of-the-month dt)
        ldom-dow (t/day-of-week ldom)
        ldom-dow-diff (- ldom-dow (:weekday rd))
        period (if (>= ldom-dow-diff 0) (dec (:period rd)) (:period rd))
        days-to-sub (+ (* period 7) ldom-dow-diff)
        days (- (t/day ldom) days-to-sub)]
    (date-constructor dt (t/year dt) (t/month dt) days)))
(defmethod rdate-add [:clj-rdate.core/first-day-of-month :clj-rdate.core/date-obj] [rd dt]
  (t/first-day-of-the-month dt))
(defmethod rdate-add [:clj-rdate.core/last-day-of-month :clj-rdate.core/date-obj] [rd dt]
  (t/last-day-of-the-month dt))
(defmethod rdate-add [:clj-rdate.core/easter-sunday :clj-rdate.core/date-obj] [rd dt]
  (intfn/easter-sunday date-constructor dt (:period rd)))
(defmethod rdate-add [:clj-rdate.core/day-month :clj-rdate.core/date-obj] [rd dt]
  (date-constructor dt (t/year dt) (:month rd) (:day rd)))
(defmethod rdate-add [:clj-rdate.core/compound :clj-rdate.core/date-obj] [rd dt]
  (reduce #(rdate-add %2 %1) dt (:parts rd)))
(defmethod rdate-add [:clj-rdate.core/repeat :clj-rdate.core/date-obj] [rd dt]
  (reduce #(rdate-add %2 %1) dt (repeat (:times rd) (:part rd))))
(defmethod rdate-add [:clj-rdate.core/calendar :clj-rdate.core/date-obj] [rd dt]
  (let [bdc-op (get {:clj-rdate.core/nbd t/plus :clj-rdate.core/pbd t/minus} (:bdc rd))
        cal (:cal rd)
        one-day (t/days 1)]
    (loop [result (rdate-add (:rdate rd) dt)]
      (if (is-not-holiday? cal result) result (recur (bdc-op result one-day))))))
