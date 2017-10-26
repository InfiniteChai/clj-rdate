(ns clj-rdate.core
  (:require [clj-time.core :as t]
    [instaparse.core :as insta]
    [instaparse.combinators :as c]))

(def rdate-parser (insta/parser {
  :rdate-token (c/alt (c/nt :days) (c/nt :weeks) (c/nt :months) (c/nt :years)
    (c/nt :weekdays) (c/nt :nth-weekdays) (c/nt :nth-last-weekdays) (c/nt :first-day-of-month) (c/nt :last-day-of-month))
  :days (c/cat (c/nt :int) (c/hide (c/string "d")))
  :weeks (c/cat (c/nt :int) (c/hide (c/string "w")))
  :months (c/cat (c/nt :int) (c/hide (c/string "m")))
  :years (c/cat (c/nt :int) (c/hide (c/string "y")))
  :first-day-of-month (c/hide (c/string "FDOM"))
  :last-day-of-month (c/hide (c/string "LDOM"))
  :weekdays (c/cat (c/nt :non-zero-int) (c/regexp #"MON|TUE|WED|THU|FRI|SAT|SUN"))
  :nth-weekdays (c/cat (c/regexp #"1st|2nd|3rd|4th|5th") (c/hide (c/string " ")) (c/regexp #"MON|TUE|WED|THU|FRI|SAT|SUN"))
  :nth-last-weekdays (c/cat (c/regexp #"Last|2nd Last|3rd Last|4th Last|5th Last") (c/hide (c/string " ")) (c/regexp #"MON|TUE|WED|THU|FRI|SAT|SUN"))
  :int (c/hide-tag (c/regexp #"-?[0-9]+"))
  :non-zero-int (c/hide-tag (c/regexp #"-?[1-9][0-9]*"))} :start :rdate-token))

(defn rdate [repr] (insta/transform {
  :days (fn [period] {:type :days :period (Integer. period)})
  :weeks (fn [period] {:type :weeks :period (Integer. period)})
  :months (fn [period] {:type :months :period (Integer. period)})
  :years (fn [period] {:type :years :period (Integer. period)})
  :first-day-of-month (fn [] {:type :first-day-of-month})
  :last-day-of-month (fn [] {:type :last-day-of-month})
  :weekdays (fn [period weekday] {:type :weekdays :period (Integer. period) :weekday (get {"MON" 1 "TUE" 2 "WED" 3 "THU" 4 "FRI" 5 "SAT" 6 "SUN" 7} weekday)})
  :nth-weekdays (fn [period weekday] {:type :nth-weekdays
    :period (get {"1st" 1 "2nd" 2 "3rd" 3 "4th" 4 "5th" 5} period)
    :weekday (get {"MON" 1 "TUE" 2 "WED" 3 "THU" 4 "FRI" 5 "SAT" 6 "SUN" 7} weekday)})
  :nth-last-weekdays (fn [period weekday] {:type :nth-last-weekdays
    :period (get {"Last" 1 "2nd Last" 2 "3rd Last" 3 "4th Last" 4 "5th Last" 5} period)
    :weekday (get {"MON" 1 "TUE" 2 "WED" 3 "THU" 4 "FRI" 5 "SAT" 6 "SUN" 7} weekday)})

  :rdate-token identity
  } (rdate-parser repr)))

(defmulti date-constructor (fn [current_dt year month day] (class current_dt)))
(defmethod date-constructor org.joda.time.DateTime [current_dt year month day]
  (t/date-time year month day))
(defmethod date-constructor org.joda.time.LocalDate [current_dt year month day]
  (t/local-date year month day))

(defmulti rdate-add (fn [rd dt] (:type rd)))

(defmethod rdate-add :months [rd dt]
  (t/plus dt (t/months (:period rd))))
(defmethod rdate-add :days [rd dt]
  (t/plus dt (t/days (:period rd))))
(defmethod rdate-add :weeks [rd dt]
  (t/plus dt (t/weeks (:period rd))))
(defmethod rdate-add :years [rd dt]
  "Nth year from today"
  (t/plus dt (t/years (:period rd))))
(defmethod rdate-add :weekdays [rd dt]
  "Get the nth weekday from today (add or subtract)"
  (let [weekday-diff (- (t/day-of-week dt) (:weekday rd))
        period-chg (cond (and (< (:period rd) 0) (> weekday-diff 0)) 1 (and (> (:period rd) 0) (< weekday-diff 0)) -1 :else 0)
        period (+ (:period rd) period-chg)]
      (t/plus dt (t/days (- (* period 7) weekday-diff)))))
(defmethod rdate-add :nth-weekdays [rd dt]
  "Get the nth weekday in the given month. Exception if out of bounds"
  (let [wkd (t/day-of-week dt)
        wkd-1st (inc (mod (- (dec wkd) (dec (mod (t/day dt) 7))) 7))
        wkd-1st-diff (- wkd-1st (:weekday rd))
        period (if (> wkd-1st-diff 0) (:period rd) (dec (:period rd)))
        days (inc (- (* 7 period) wkd-1st-diff))]
    (date-constructor dt (t/year dt) (t/month dt) days)))
(defmethod rdate-add :nth-last-weekdays [rd dt]
  "Get the nth last weekday in the given month. Exception if out of bounds"
  (let [ldom (t/last-day-of-the-month dt)
        ldom-dow (t/day-of-week ldom)
        ldom-dow-diff (- ldom-dow (:weekday rd))
        period (if (>= ldom-dow-diff 0) (dec (:period rd)) (:period rd))
        days-to-sub (+ (* period 7) ldom-dow-diff)
        days (- (t/day ldom) days-to-sub)]
    (date-constructor dt (t/year dt) (t/month dt) days)))
    
(defmethod rdate-add :first-day-of-month [rd dt]
  (t/first-day-of-the-month dt))
(defmethod rdate-add :last-day-of-month [rd dt]
  (t/last-day-of-the-month dt))
