# clj-rdate

A relative date library for Clojure, wrapping the [clj-time](https://github.com/clj-time/clj-time) library
to allow for relative manipulation of dates.

## Usage

### clj-rdate.core

The main namespace for relative date operations in the `clj-rdate` library is `clj-rdate.core`.
``` clj
(require '[clj-rdate.core :as rd])
```

A relative date is commonly parsed from a string definition
``` clj
(rd/rdate "1d")
=> {:type :days, :period 1}
```  

And we can add these to dates from the clj-time library.
``` clj
(require '[clj-time.core :as t])
(rd/rdate-add (rd/rdate "1d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-26"]
```

Its also worth noting that this will work on different date formats supported
by the clj-time library.
``` clj
(rd/rdate-add (rd/rdate "1d") (t/date-time 2017 10 25))
=> #[org.joda.time.DateTime "2017-10-26T00:00:00.000Z"]
```
### Relative Dates

#### Days

Allows for the addition/subtraction of a number of days from a given date.
``` clj
(rd/rdate-add (rd/rdate "8d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-11-02"]
(rd/rdate-add (rd/rdate "-8d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-17"]
```

`0d` is a valid relative date format and should give you back the same day
``` clj
(rd/rdate-add (rd/rdate "0d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-25"]
```

#### Weeks

Allows for the addition/subtraction of a number of weeks from a given date.
``` clj
(rd/rdate-add (rd/rdate "8w") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-12-20"]
(rd/rdate-add (rd/rdate "-8w") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-08-30"]
```

`0w` is a valid relative date format and should give you back the same day
``` clj
(rd/rdate-add (rd/rdate "0w") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-25"]
```

#### Months

Allows for the addition/subtraction of a number of months from a given date.
``` clj
(rd/rdate-add (rd/rdate "2m") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-12-25"]
(rd/rdate-add (rd/rdate "-2m") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-08-25"]
```

`0m` is a valid relative date format and should give you back the same day
``` clj
(rd/rdate-add (rd/rdate "0m") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-25"]
```

In cases when the the increment of the months would not support a real date then it will fall back to the last day of the month.
``` clj
(rd/rdate-add (rd/rdate "1m") (t/local-date 2017 10 31))
=> #[org.joda.time.LocalDate "2017-11-30"]
```

#### Years

Allows for the addition/subtraction of a number of years from a given date.
``` clj
(rd/rdate-add (rd/rdate "2y") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2019-10-25"]
(rd/rdate-add (rd/rdate "-2y") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2015-10-25"]
```

`0y` is a valid relative date format and should give you back the same day
``` clj
(rd/rdate-add (rd/rdate "0y") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-25"]
```

In case of a leap year and increments from the 29th Feb, then if we increment to a non-leap year, it will fall on the 28th Feb.
``` clj
(rd/rdate-add (rd/rdate "1y") (t/local-date 2012 2 29))
=> #[org.joda.time.LocalDate "2013-02-28"]
```

#### Weekdays

The relative date supports the ability to find the nth weekday from the current day. If we want to know the next Saturday then we can do the following
``` clj
(rd/rdate-add (rd/rdate "1SAT") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-28"]
```

To know the last Tuesday from a given date, then we can use a negative value
``` clj
(rd/rdate-add (rd/rdate "-1TUE") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-24"]
```

#### Nth Weekdays

Find the nth weekday in the given month of the date passed in. If the date
doesn't exist, then an exception will be thrown
``` clj
(rd/rdate-add (rd/rdate "1st THU") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-05"]
(rd/rdate-add (rd/rdate "3rd MON") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-16"]
(rd/rdate-add (rd/rdate "5th FRI") (t/local-date 2017 10 25))
=> IllegalFieldValueException Value 34 for dayOfMonth must be in the range [1,31]
```

## License

Released under the MIT License: <https://github.com/InfiniteChai/clj-rdate/blob/master/MIT-LICENSE.txt>
