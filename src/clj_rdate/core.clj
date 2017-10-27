(ns clj-rdate.core
  (:require [clj-time.core :as t]
            [clj-rdate.internal.fn :as intfn]
            [instaparse.core :as insta]
            [instaparse.combinators :as c]
            [clj-rdate.holiday-cal :as cal]
            [clj-rdate.internal.rdate-defns :as rd-defns :refer :all]))

(def rdate-parser "The grammar definition for an rdate" (insta/parser
  "rdate-expr = add-sub
   <add-sub> = mult | add | sub
   add = add-sub <'+'> mult
   sub = add-sub <'-'> mult
   <mult> = rdate | left-mult | right-mult
   left-mult = pos-int <'*'> mult
   right-mult = mult <'*'> pos-int
   <rdate> = rdate-term | <'('> add-sub <')'>
   rdate-term = days | weeks | months | years | easter-sunday | weekdays | nth-weekdays | nth-last-weekdays | first-day-of-month | last-day-of-month | day-month | calendar
   <cal-rdate-term> = days | weeks | months | years
   calendar = cal-rdate-term <'@'> #'(\\w|\\||\\s)+\\w'
   days = int <'d'>
   weeks = int <'w'>
   months = int <'m'>
   years = int <'y'>
   day-month = pos-int month-short
   easter-sunday = int <'E'>
   first-day-of-month = <'FDOM'>
   last-day-of-month = <'LDOM'>
   weekdays = non-zero-int weekday-short
   nth-weekdays = #'1st|2nd|3rd|4th|5th' <' '> weekday-short
   nth-last-weekdays = #'Last|2nd Last|3rd Last|4th Last|5th Last' <' '> weekday-short
   <int> = #'-?[0-9]+'
   <pos-int> = #'[1-9][0-9]*'
   <non-zero-int> = #'-?[1-9][0-9]*'
   <month-short> = #'JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC'
   <weekday-short> = #'MON|TUE|WED|THU|FRI|SAT|SUN'"
   :start :rdate-expr
   :auto-whitespace :standard))

(defn rdate [repr] (insta/transform {
  :days (fn [p] {:type ::days :period (Integer. p)})
  :weeks (fn [p] {:type ::weeks :period (Integer. p)})
  :months (fn [p] {:type ::months :period (Integer. p)})
  :years (fn [p] {:type ::years :period (Integer. p)})
  :first-day-of-month (fn [] {:type ::first-day-of-month})
  :last-day-of-month (fn [] {:type ::last-day-of-month})
  :easter-sunday (fn [p] {:type ::easter-sunday :period (Integer. p)})
  :add (fn [l r] {:type ::compound :parts [l r]})
  :sub (fn [l r] {:type ::compound :parts [l (rdate-neg r)]})
  :left-mult (fn [c rd] {:type ::repeat :times (Integer. c) :part rd})
  :right-mult (fn [rd c] {:type ::repeat :times (Integer. c) :part rd})
  :weekdays (fn [p wd] {:type ::weekdays :period (Integer. p) :weekday (get weekdays-short wd)})
  :day-month (fn [d m] {:type ::day-month :day (Integer. d) :month (get months-short m)})
  :nth-weekdays (fn [p w] {:type ::nth-weekdays
    :period (get {"1st" 1 "2nd" 2 "3rd" 3 "4th" 4 "5th" 5} p)
    :weekday (get weekdays-short w)})
  :nth-last-weekdays (fn [p w] {:type ::nth-last-weekdays
    :period (get {"Last" 1 "2nd Last" 2 "3rd Last" 3 "4th Last" 4 "5th Last" 5} p)
    :weekday (get weekdays-short w)})
  :calendar (fn [rd c] {:type ::calendar :rdate rd :cal (cal/get-calendar c)
    :bdc (if (rdate-is-neg? rd) ::pbd ::nbd)})
  :rdate-expr identity
  :rdate-term identity
  } (rdate-parser repr)))

; rdate-neg should give back the appropriate negation of the given rdate.
; NOTE: There should be no requirement for rd-rd to be equivalent to 0d, as There
; are obvious examples where this is not the case
(defmethod rdate-neg ::days [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg ::weeks [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg ::months [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg ::years [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg ::weekdays [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg ::easter-sunday [rd] (update-in rd [:period] * -1))

(defmethod rdate-is-neg? ::days [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? ::weeks [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? ::months [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? ::years [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? ::weekdays [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? ::easter-sunday [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? ::rdate [rd] false)

; The variety of addition methods for rdates with dates or other rdates
(defmethod rdate-add [::date-obj ::rdate] [dt rd] (rdate-add rd dt))
(defmethod rdate-add [::rdate ::rdate] [left right]
  {:type ::compound :parts [left right]})
(defmethod rdate-add [::string-obj ::date-obj] [rd dt] (rdate-add (rdate rd) dt))
(defmethod rdate-add [::date-obj ::string-obj] [dt rd] (rdate-add (rdate rd) dt))
(defmethod rdate-add [::string-obj ::string-obj] [l r] (rdate-add (rdate l) (rdate r)))
(defmethod rdate-add [::days ::date-obj] [rd dt]
  (t/plus dt (t/days (:period rd))))
(defmethod rdate-add [::weeks ::date-obj] [rd dt]
  (t/plus dt (t/weeks (:period rd))))
(defmethod rdate-add [::months ::date-obj] [rd dt]
  (t/plus dt (t/months (:period rd))))
(defmethod rdate-add [::years ::date-obj] [rd dt]
  (t/plus dt (t/years (:period rd))))
(defmethod rdate-add [::weekdays ::date-obj] [rd dt]
  (let [weekday-diff (- (t/day-of-week dt) (:weekday rd))
        period-chg (cond (and (< (:period rd) 0) (> weekday-diff 0)) 1 (and (> (:period rd) 0) (< weekday-diff 0)) -1 :else 0)
        period (+ (:period rd) period-chg)]
      (t/plus dt (t/days (- (* period 7) weekday-diff)))))
(defmethod rdate-add [::nth-weekdays ::date-obj] [rd dt]
  "Get the nth weekday in the given month. Exception if out of bounds"
  (let [wkd (t/day-of-week dt)
        wkd-1st (inc (mod (- (dec wkd) (dec (mod (t/day dt) 7))) 7))
        wkd-1st-diff (- wkd-1st (:weekday rd))
        period (if (> wkd-1st-diff 0) (:period rd) (dec (:period rd)))
        days (inc (- (* 7 period) wkd-1st-diff))]
    (date-constructor dt (t/year dt) (t/month dt) days)))
(defmethod rdate-add [::nth-last-weekdays ::date-obj] [rd dt]
  "Get the nth last weekday in the given month. Exception if out of bounds"
  (let [ldom (t/last-day-of-the-month dt)
        ldom-dow (t/day-of-week ldom)
        ldom-dow-diff (- ldom-dow (:weekday rd))
        period (if (>= ldom-dow-diff 0) (dec (:period rd)) (:period rd))
        days-to-sub (+ (* period 7) ldom-dow-diff)
        days (- (t/day ldom) days-to-sub)]
    (date-constructor dt (t/year dt) (t/month dt) days)))
(defmethod rdate-add [::first-day-of-month ::date-obj] [rd dt]
  (t/first-day-of-the-month dt))
(defmethod rdate-add [::last-day-of-month ::date-obj] [rd dt]
  (t/last-day-of-the-month dt))
(defmethod rdate-add [::easter-sunday ::date-obj] [rd dt]
  (intfn/easter-sunday date-constructor dt (:period rd)))
(defmethod rdate-add [::day-month ::date-obj] [rd dt]
  (date-constructor dt (t/year dt) (:month rd) (:day rd)))
(defmethod rdate-add [::compound ::date-obj] [rd dt]
  (reduce #(rdate-add %2 %1) dt (:parts rd)))
(defmethod rdate-add [::repeat ::date-obj] [rd dt]
  (reduce #(rdate-add %2 %1) dt (repeat (:times rd) (:part rd))))
(defmethod rdate-add [::calendar ::date-obj] [rd dt]
  (let [bdc-op (get {::nbd t/plus ::pbd t/minus} (:bdc rd))
        cal (:cal rd)
        one-day (t/days 1)]
    (loop [result (rdate-add (:rdate rd) dt)]
      (if (cal/is-not-holiday? cal result) result (recur (bdc-op result one-day))))))
