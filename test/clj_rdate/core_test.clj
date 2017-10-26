(ns clj-rdate.core-test
  (:require [clojure.test :refer :all]
            [clj-rdate.core :refer :all]
            [clj-time.core :as t]))

(deftest test-rdate-add-days
  (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
    ; First check basic cases up add and subtract work
    (t/local-date 2017 10 26) {:rd "1d" :dt (t/local-date 2017 10 25)}
    (t/local-date 2017 10 24) {:rd "-1d" :dt (t/local-date 2017 10 25)}
    ; Expect no change with 0d
    (t/local-date 2017 10 25) {:rd "0d" :dt (t/local-date 2017 10 25)}
    ; Check that we correctly increment months
    (t/local-date 2017 11 1) {:rd "7d" :dt (t/local-date 2017 10 25)}
    ; Check that we don't have any weekend handling
    (t/local-date 2017 10 28) {:rd "1d" :dt (t/local-date 2017 10 27)}

    (t/date-time 2017 10 26) {:rd "1d" :dt (t/date-time 2017 10 25)}
    (t/date-time 2017 10 24) {:rd "-1d" :dt (t/date-time 2017 10 25)}
    ; Expect no change with 0d
    (t/date-time 2017 10 25) {:rd "0d" :dt (t/date-time 2017 10 25)}
    ; Check that we correctly increment months
    (t/date-time 2017 11 1) {:rd "7d" :dt (t/date-time 2017 10 25)}
    ; Check that we don't have any weekend handling
    (t/date-time 2017 10 28) {:rd "1d" :dt (t/date-time 2017 10 27)}))

(deftest test-rdate-add-weeks
  (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
    ; First check basic cases up add and subtract work
    (t/local-date 2017 11 1) {:rd "1w" :dt (t/local-date 2017 10 25)}
    (t/local-date 2017 10 18) {:rd "-1w" :dt (t/local-date 2017 10 25)}
    ; Expect no change with 0w
    (t/local-date 2017 10 25) {:rd "0w" :dt (t/local-date 2017 10 25)}
    ; Check that we correctly increment months
    (t/local-date 2017 12 13) {:rd "7w" :dt (t/local-date 2017 10 25)}

    (t/date-time 2017 11 1) {:rd "1w" :dt (t/date-time 2017 10 25)}
    (t/date-time 2017 10 18) {:rd "-1w" :dt (t/date-time 2017 10 25)}
    ; Expect no change with 0w
    (t/date-time 2017 10 25) {:rd "0w" :dt (t/date-time 2017 10 25)}
    ; Check that we correctly increment months
    (t/date-time 2017 12 13) {:rd "7w" :dt (t/date-time 2017 10 25)}))


(deftest test-rdate-add-months
    (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
      ; First check basic cases up add and subtract work
      (t/local-date 2017 11 25) {:rd "1m" :dt (t/local-date 2017 10 25)}
      (t/local-date 2017 9 25) {:rd "-1m" :dt (t/local-date 2017 10 25)}
      ; Expect no change with 0m
      (t/local-date 2017 10 25) {:rd "0m" :dt (t/local-date 2017 10 25)}
      ; Cbeck that we preserve the month and maintain last day in conflict
      (t/local-date 2017 11 30) {:rd "1m" :dt (t/local-date 2017 10 31)}
      (t/local-date 2017 9 30) {:rd "-1m" :dt (t/local-date 2017 10 31)}
      ; Check that holds true on leap years as well
      (t/local-date 2013 2 28) {:rd "12m" :dt (t/local-date 2012 2 29)}
      ; Cbeck that we preserve the month and maintain last day in conflict
      (t/local-date 2011 2 28) {:rd "-12m" :dt (t/local-date 2012 2 29)}

      (t/date-time 2017 11 25) {:rd "1m" :dt (t/date-time 2017 10 25)}
      (t/date-time 2017 9 25) {:rd "-1m" :dt (t/date-time 2017 10 25)}
      ; Expect no change with 0m
      (t/date-time 2017 10 25) {:rd "0m" :dt (t/date-time 2017 10 25)}
      ; Cbeck that we preserve the month and maintain last day in conflict
      (t/date-time 2017 11 30) {:rd "1m" :dt (t/date-time 2017 10 31)}
      (t/date-time 2017 9 30) {:rd "-1m" :dt (t/date-time 2017 10 31)}
      ; Check that holds true on leap years as well
      (t/date-time 2013 2 28) {:rd "12m" :dt (t/date-time 2012 2 29)}
      ; Cbeck that we preserve the month and maintain last day in conflict
      (t/date-time 2011 2 28) {:rd "-12m" :dt (t/date-time 2012 2 29)}))

(deftest test-rdate-add-years
    (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
      ; First check basic cases up add and subtract work
      (t/local-date 2018 10 25) {:rd "1y" :dt (t/local-date 2017 10 25)}
      (t/local-date 2016 10 25) {:rd "-1y" :dt (t/local-date 2017 10 25)}
      (t/local-date 2029 10 25) {:rd "12y" :dt (t/local-date 2017 10 25)}
      (t/local-date 2005 10 25) {:rd "-12y" :dt (t/local-date 2017 10 25)}
      ; Expect no change with 0y
      (t/local-date 2017 10 25) {:rd "0y" :dt (t/local-date 2017 10 25)}
      ; Check that we preserve the month and use last business day on leap years in conflict
      (t/local-date 2013 2 28) {:rd "1y" :dt (t/local-date 2012 2 29)}
      (t/local-date 2011 2 28) {:rd "-1y" :dt (t/local-date 2012 2 29)}
      (t/date-time 2018 10 25) {:rd "1y" :dt (t/date-time 2017 10 25)}
      (t/date-time 2016 10 25) {:rd "-1y" :dt (t/date-time 2017 10 25)}
      (t/date-time 2029 10 25) {:rd "12y" :dt (t/date-time 2017 10 25)}
      (t/date-time 2005 10 25) {:rd "-12y" :dt (t/date-time 2017 10 25)}
      ; Expect no change with 0y
      (t/date-time 2017 10 25) {:rd "0y" :dt (t/date-time 2017 10 25)}
      ; Check that we preserve the month and use last business day on leap years in conflict
      (t/date-time 2013 2 28) {:rd "1y" :dt (t/date-time 2012 2 29)}
      (t/date-time 2011 2 28) {:rd "-1y" :dt (t/date-time 2012 2 29)}))

(deftest test-rdate-add-weekdays
  (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
    ; First check basic cases up add and subtract work
    (t/local-date 2017 10 30) {:rd "1MON" :dt (t/local-date 2017 10 25)}
    (t/local-date 2017 12 30) {:rd "10SAT" :dt (t/local-date 2017 10 25)}
    (t/local-date 2017 10 18) {:rd "-1WED" :dt (t/local-date 2017 10 25)}
    (t/local-date 2017 8 18) {:rd "-10FRI" :dt (t/local-date 2017 10 25)}

    (t/date-time 2017 10 30) {:rd "1MON" :dt (t/date-time 2017 10 25)}
    (t/date-time 2017 12 30) {:rd "10SAT" :dt (t/date-time 2017 10 25)}
    (t/date-time 2017 10 18) {:rd "-1WED" :dt (t/date-time 2017 10 25)}
    (t/date-time 2017 8 18) {:rd "-10FRI" :dt (t/date-time 2017 10 25)}))

(deftest test-rdate-add-nth-weekdays
  (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
    ; First check basic cases up add and subtract work
    (t/local-date 2017 10 2) {:rd "1st MON" :dt (t/local-date 2017 10 25)}
    (t/local-date 2017 10 13) {:rd "2nd FRI" :dt (t/local-date 2017 10 25)}
    (t/local-date 2017 11 25) {:rd "4th SAT" :dt (t/local-date 2017 11 25)}
    (t/local-date 2017 12 31) {:rd "5th SUN" :dt (t/local-date 2017 12 25)}

    (t/date-time 2017 10 2) {:rd "1st MON" :dt (t/date-time 2017 10 25)}
    (t/date-time 2017 10 13) {:rd "2nd FRI" :dt (t/date-time 2017 10 25)}
    (t/date-time 2017 11 25) {:rd "4th SAT" :dt (t/date-time 2017 11 25)}
    (t/date-time 2017 12 31) {:rd "5th SUN" :dt (t/date-time 2017 12 25)}))

(deftest test-rdate-add-nth-last-weekdays
  (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
    ; First check basic cases up add and subtract work
    (t/local-date 2017 10 30) {:rd "Last MON" :dt (t/local-date 2017 10 24)}
    (t/local-date 2017 10 20) {:rd "2nd Last FRI" :dt (t/local-date 2017 10 24)}
    (t/local-date 2017 12 03) {:rd "5th Last SUN" :dt (t/local-date 2017 12 24)}

    (t/date-time 2017 10 30) {:rd "Last MON" :dt (t/date-time 2017 10 24)}
    (t/date-time 2017 10 20) {:rd "2nd Last FRI" :dt (t/date-time 2017 10 24)}
    (t/date-time 2017 12 03) {:rd "5th Last SUN" :dt (t/date-time 2017 12 24)}))

(deftest test-rdate-add-nth-weekdays-bad
  (are [args] (thrown? Exception (rdate-add (rdate (:rd args)) (:dt args)))
    ;  Run through cases where there is no 5th weekday in a given month
    {:rd "5th WED" :dt (t/local-date 2017 10 25)}
    {:rd "5th MON" :dt (t/local-date 2017 6 1)}

    {:rd "5th WED" :dt (t/date-time 2017 10 25)}
    {:rd "5th MON" :dt (t/date-time 2017 6 1)}))

(deftest test-rdate-add-nth-last-weekdays-bad
  (are [args] (thrown? Exception (rdate-add (rdate (:rd args)) (:dt args)))
    ;  Run through cases where there is no 5th weekday in a given month
    {:rd "5th Last WED" :dt (t/local-date 2017 10 25)}
    {:rd "5th Last MON" :dt (t/local-date 2017 6 1)}

    {:rd "5th Last WED" :dt (t/date-time 2017 10 25)}
    {:rd "5th Last MON" :dt (t/date-time 2017 6 1)}))

(deftest test-rdate-add-first-day-of-month
  (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
    ; First check basic cases up add and subtract work
    (t/local-date 2017 10 1) {:rd "FDOM" :dt (t/local-date 2017 10 25)}
    (t/date-time 2017 10 1) {:rd "FDOM" :dt (t/date-time 2017 10 25)}))

(deftest test-rdate-add-last-day-of-month
  (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
    ; First check basic cases up add and subtract work
    (t/local-date 2017 10 31) {:rd "LDOM" :dt (t/local-date 2017 10 25)}
    (t/date-time 2017 10 31) {:rd "LDOM" :dt (t/date-time 2017 10 25)}))

(deftest test-rdate-add-basic-addition-compounds
  (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
    ; Start with some simple models
    (t/local-date 2017 10 28) {:rd "1d+1d" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 30) {:rd "1d+1d+1d+1d" :dt (t/local-date 2017 10 26)}
    ; Some trivial no-op scenarios
    (t/local-date 2017 10 26) {:rd "1d-1d" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 26) {:rd "1d-1d-1d+1d" :dt (t/local-date 2017 10 26)}
    ; And check more complex no-ops with unary operators
    (t/local-date 2017 10 26) {:rd "-1d+3d-2d" :dt (t/local-date 2017 10 26)}
    ; Check that mixing methods and ordering works as expected (left to right)
    (t/local-date 2017 10 19) {:rd "3rd WED+1d" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 18) {:rd "1d+3rd WED" :dt (t/local-date 2017 10 26)}
    ; Now check some more obscure examples where they're not 'no-op'
    (t/local-date 2017 10 26) {:rd "1m-1m" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 30) {:rd "1m-1m" :dt (t/local-date 2017 10 31)}
    ; Start with some simple models
    (t/date-time 2017 10 28) {:rd "1d+1d" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 30) {:rd "1d+1d+1d+1d" :dt (t/date-time 2017 10 26)}
    ; Some trivial no-op scenarios
    (t/date-time 2017 10 26) {:rd "1d-1d" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 26) {:rd "1d-1d-1d+1d" :dt (t/date-time 2017 10 26)}
    ; And check more complex no-ops with unary operators
    (t/date-time 2017 10 26) {:rd "-1d+3d-2d" :dt (t/date-time 2017 10 26)}
    ; Check that mixing methods and ordering works as expected (left to right)
    (t/date-time 2017 10 19) {:rd "3rd WED+1d" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 18) {:rd "1d+3rd WED" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 26) {:rd "1m-1m" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 30) {:rd "1m-1m" :dt (t/date-time 2017 10 31)}))


(deftest test-rdate-add-basic-multiplication-compounds
  (are [exp args] (= exp (rdate-add (rdate (:rd args)) (:dt args)))
    ; Start with some simple left multiplier cases
    (t/local-date 2017 10 27) {:rd "1*1d" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 28) {:rd "2*1d" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 30) {:rd "4*1d" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 22) {:rd "4*-1d" :dt (t/local-date 2017 10 26)}
    ; And now check the right multiplier
    (t/local-date 2017 10 27) {:rd "1d*1" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 28) {:rd "1d*2" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 30) {:rd "1d*4" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 10 22) {:rd "-1d*4" :dt (t/local-date 2017 10 26)}
    ; Repeated multiplications work as expected? 3*2*1d == 6d and check
    ; various iterations work as expected
    (t/local-date 2017 11 01) {:rd "3*2*1d" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 11 01) {:rd "1d*2*3" :dt (t/local-date 2017 10 26)}
    (t/local-date 2017 11 01) {:rd "2*1d*3" :dt (t/local-date 2017 10 26)}

    ; Check that it takes prescedence over addition
    (t/local-date 2017 11 02) {:rd "2*3d+1d" :dt (t/local-date 2017 10 26)}
    ; But bracketing will overrule this
    (t/local-date 2017 11 03) {:rd "2*(3d+1d)" :dt (t/local-date 2017 10 26)}
    ; Start with some simple left multiplier cases
    (t/date-time 2017 10 27) {:rd "1*1d" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 28) {:rd "2*1d" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 30) {:rd "4*1d" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 22) {:rd "4*-1d" :dt (t/date-time 2017 10 26)}
    ; And now check the right multiplier
    (t/date-time 2017 10 27) {:rd "1d*1" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 28) {:rd "1d*2" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 30) {:rd "1d*4" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 10 22) {:rd "-1d*4" :dt (t/date-time 2017 10 26)}
    ; Repeated multiplications work as expected? 3*2*1d == 6d and check
    ; various iterations work as expected
    (t/date-time 2017 11 01) {:rd "3*2*1d" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 11 01) {:rd "1d*2*3" :dt (t/date-time 2017 10 26)}
    (t/date-time 2017 11 01) {:rd "2*1d*3" :dt (t/date-time 2017 10 26)}

    ; Check that it takes prescedence over addition
    (t/date-time 2017 11 02) {:rd "2*3d+1d" :dt (t/date-time 2017 10 26)}
    ; But bracketing will overrule this
    (t/date-time 2017 11 03) {:rd "2*(3d+1d)" :dt (t/date-time 2017 10 26)}))
