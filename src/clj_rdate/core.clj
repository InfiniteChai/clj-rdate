(ns clj-rdate.core
  "The core namespace for relative date operations in the clj-rdate library."
  (:require [clj-time.core :as t]
            [clj-rdate.internal.fn :as intfn]
            [instaparse.core :as insta]
            [instaparse.combinators :as c]
            [clojure.string :as s]))

(def rdate-hierarchy (-> (make-hierarchy)
  (derive ::days ::rdate)
  (derive ::weeks ::rdate)
  (derive ::months ::rdate)
  (derive ::years ::rdate)
  (derive ::weekdays ::rdate)
  (derive ::nth-weekdays ::rdate)
  (derive ::nth-last-weekdays ::rdate)
  (derive ::first-day-of-month ::rdate)
  (derive ::last-day-of-month ::rdate)
  (derive ::easter-sunday ::rdate)
  (derive ::day-month ::rdate)
  (derive ::compound ::rdate)
  (derive ::calendar ::rdate)
  (derive ::repeat ::rdate)))


(def months-short "Mapping from month short names to month integer"
  {"JAN" 1 "FEB" 2 "MAR" 3 "APR" 4 "MAY" 5 "JUN" 6 "JUL" 7 "AUG" 8 "SEP" 9 "OCT" 10 "NOV" 11 "DEC" 12})

(def weekdays-short "Mapping from weekday short names to weekday integer (starting on Mon)"
  {"MON" 1 "TUE" 2 "WED" 3 "THU" 4 "FRI" 5 "SAT" 6 "SUN" 7})

; The date contructors allow us to work back to the type of date that was passed
; in. We support the three standard date types available within clj-time
(defmulti date-constructor "Date construction wrapper for clj-time"
  (fn [current_dt year month day] (class current_dt)))
(defmethod date-constructor org.joda.time.DateTime [current_dt year month day]
  (t/date-time year month day))
(defmethod date-constructor org.joda.time.LocalDate [current_dt year month day]
  (t/local-date year month day))
(defmethod date-constructor org.joda.time.LocalDateTime [current_dt year month day]
  (t/local-date-time year month day))

; Simple type function for allowing generalised methods that take rdates or dates
(defmulti rdate-arg-type "Retrieve the type for dispatch" class)
(defmethod rdate-arg-type clojure.lang.PersistentArrayMap [rd] (:type rd))
(defmethod rdate-arg-type org.joda.time.DateTime [_] ::date-obj)
(defmethod rdate-arg-type org.joda.time.LocalDate [_] ::date-obj)
(defmethod rdate-arg-type org.joda.time.LocalDateTime [_] ::date-obj)
(defmethod rdate-arg-type java.lang.String [_] ::string-obj)

; The public APIs for supporting rdate manipulation
(defmulti rdate-neg "Retrieve the negation of the given rdate" :type)
(defmulti rdate-is-neg? "Check whether a given rdate would be deemed 'negative'" :type :hierarchy #'rdate-hierarchy)
(defmulti rdate-add "rdate addition method for combining rdates with dates (or rdates)"
  (fn [l r] [(rdate-arg-type l) (rdate-arg-type r)]) :hierarchy #'rdate-hierarchy)

; Various calenkdar related methods
(defmulti get-calendar (fn [cal-name] (if (s/includes? cal-name "|") :multi-part cal-name)))
(defmulti is-holiday? (fn [cal dt] (:type cal)))
(defn is-not-holiday? [cal dt] (not (is-holiday? cal dt)))
(defmulti holidays (fn [cal from-dt to-dt] (:type cal)))

(def rdate-parser "The grammar definition for an rdate" (insta/parser
  "rdate-expr = add-sub
   <add-sub> = mult | add | sub
   add = add-sub <'+'> mult
   sub = add-sub <'-'> mult
   <mult> = rdate | left-mult | right-mult
   left-mult = pos-int <'*'> mult
   right-mult = mult <'*'> pos-int
   <rdate> = rdate-term | <'('> add-sub <')'>
   rdate-term = biz-days | biz-days-with-cal | days | weeks | months | years | easter-sunday | weekdays | nth-weekdays | nth-last-weekdays | first-day-of-month | last-day-of-month | day-month | calendar
   <cal-rdate-term> = days | weeks | months | years
   calendar = cal-rdate-term <'@'> cal
   biz-days = int <'b'>
   biz-days-with-cal = int <'b'> <'@'> cal
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
   <cal> = #'(\\w|\\||\\s)+\\w'
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
  :calendar (fn [rd c] {:type ::calendar :rdate rd :cal (get-calendar c)
    :bdc (if (rdate-is-neg? rd) ::pbd ::nbd)})
  :biz-days (fn [p]
    (let [period (Integer. p)
          repeat-count (Integer. (max 1 (Math/abs period)))
          day-period (Integer/signum period)
          bdc (if (< day-period 0) ::pbd ::nbd)]
          {:type ::repeat
           :times repeat-count
           :part {:type ::calendar
                  :rdate {:type ::days
                          :period day-period}
                  :bdc bdc
                  :cal (get-calendar "Weekdays")}}))
  :biz-days-with-cal (fn [p cal]
    (let [period (Integer. p)
          repeat-count (Integer. (max 1 (Math/abs period)))
          day-period (Integer/signum period)
          bdc (if (< day-period 0) ::pbd ::nbd)]
      {:type ::repeat
       :times repeat-count
       :part {:type ::calendar
              :rdate {:type ::days
                      :period day-period}
              :bdc bdc
              :cal (get-calendar cal)}}))
  :rdate-expr identity
  :rdate-term identity
  } (rdate-parser repr)))

(require 'clj-rdate.internal.rdate-add-impl
         'clj-rdate.internal.rdate-neg-impl
         'clj-rdate.internal.holiday-cal-impl)
