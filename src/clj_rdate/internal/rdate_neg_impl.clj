(ns clj-rdate.internal.rdate-neg-impl
  "Implementation details for rd/rdate-neg and associated methods"
  (:require [clj-time.core :as t]))

(refer 'clj-rdate.core)

(defmethod rdate-neg :clj-rdate.core/days [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg :clj-rdate.core/weeks [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg :clj-rdate.core/months [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg :clj-rdate.core/years [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg :clj-rdate.core/weekdays [rd] (update-in rd [:period] * -1))
(defmethod rdate-neg :clj-rdate.core/easter-sunday [rd] (update-in rd [:period] * -1))

(defmethod rdate-is-neg? :clj-rdate.core/days [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? :clj-rdate.core/weeks [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? :clj-rdate.core/months [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? :clj-rdate.core/years [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? :clj-rdate.core/weekdays [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? :clj-rdate.core/easter-sunday [rd] (< (:period rd) 0))
(defmethod rdate-is-neg? :clj-rdate.core/rdate [rd] false)
