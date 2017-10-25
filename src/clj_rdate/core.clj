(ns clj-rdate.core
  (:require [clj-time.core :as t]
    [instaparse.core :as insta]
    [instaparse.combinators :as c]))

(def rdate-parser (insta/parser
  {:rdate-token (c/alt (c/nt :days) (c/nt :weeks) (c/nt :months) (c/nt :years) (c/nt :weekdays) (c/nt :nth-weekdays))
   :days (c/cat (c/nt :int) (c/hide (c/string "d")))
   :weeks (c/cat (c/nt :int) (c/hide (c/string "w")))
   :months (c/cat (c/nt :int) (c/hide (c/string "m")))
   :years (c/cat (c/nt :int) (c/hide (c/string "y")))
   :weekdays (c/cat (c/nt :non-zero-int) (c/regexp #"MON|TUE|WED|THU|FRI|SAT|SUN"))
   :nth-weekdays (c/cat (c/regexp #"1st|2nd|3rd|4th|5th") (c/hide (c/string " ")) (c/regexp #"MON|TUE|WED|THU|FRI|SAT|SUN"))
   :int (c/hide-tag (c/regexp #"-?[0-9]+"))
   :non-zero-int (c/hide-tag (c/regexp #"-?[1-9][0-9]*"))
  } :start :rdate-token))

(defn rdate [repr] (insta/transform {
  :days (fn [period] {:type :days :period (Integer. period)})
  :weeks (fn [period] {:type :weeks :period (Integer. period)})
  :months (fn [period] {:type :months :period (Integer. period)})
  :years (fn [period] {:type :years :period (Integer. period)})
  :weekdays (fn [period weekday] {:type :weekdays :period (Integer. period) :weekday (get {"MON" 1 "TUE" 2 "WED" 3 "THU" 4 "FRI" 5 "SAT" 6 "SUN" 7} weekday)})
  :nth-weekdays (fn [period weekday] {:type :nth-weekdays
    :period (get {"1st" 1 "2nd" 2 "3rd" 3 "4th" 4 "5th" 5} period)
    :weekday (get {"MON" 1 "TUE" 2 "WED" 3 "THU" 4 "FRI" 5 "SAT" 6 "SUN" 7} weekday)})
  :rdate-token identity
  } (rdate-parser repr)))


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
    (t/local-date (t/year dt) (t/month dt) days)))
