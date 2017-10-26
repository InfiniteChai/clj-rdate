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
### Relative Date Formats

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

You can also ask for the Nth Last Weekday as well, if you need to manipulate
dates in that fashion
``` clj
``` clj
(rd/rdate-add (rd/rdate "Last THU") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-26"]
(rd/rdate-add (rd/rdate "2nd Last FRI") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-20"]
```

#### First and Last Days of the Month

Given a date, this will give you back the first and last dates for
the given month
``` clj
(rd/rdate-add (rd/rdate "FDOM") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-01"]
(rd/rdate-add (rd/rdate "LDOM") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-31"]
```

#### Easter Sunday

Mainly defined to support integration with holiday calendars, but you
can find out the relative Easter Sunday from a given date. Note, `1E` refers
to the Easter Sunday of next year and not necessarily the next Easter Sunday. You can use `0E` to get this year's Easter Sunday

``` clj
(rd/rdate-add (rd/rdate "0E") (t/local-date 2017 01 25))
=> #[org.joda.time.LocalDate "2017-04-16"]
(rd/rdate-add (rd/rdate "1E") (t/local-date 2017 01 25))
=> #[org.joda.time.LocalDate "2018-04-01"]
```

#### Relative Date Algebraic Operations

The relative date library also supports addition, subtraction (where appropriate) and constant multiplication operations.

You can add two rdates together using the `+` symbol, which will be equivalent of applying the left then the right sequentially.

``` clj
(rd/rdate-add (rd/rdate "1d+2d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-28"]
(rd/rdate-add (rd/rdate "3rd WED+1d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-19"]
```

Where the rdate on the right supports negation, then you can subtract
two rdates using the `-` symbol. This is equivalent of applying the left then the negation of the right sequentially.

It's worth noting that in most cases, this is just syntax sugar of an addition and wrapping the negated right rdate in brackets.

``` clj
(rd/rdate-add (rd/rdate "1d-2d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-24"]
(rd/rdate-add (rd/rdate "3rd WED-1d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-17"]
(rd/rdate-add (rd/rdate "1d+(-2d)") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-24"]
```

Finally we also support multiplication of an rdate with a positive integer `n` using the `*` symbol. This is equivalent to repeating the rdate operation `n` times.

``` clj
(rd/rdate-add (rd/rdate "3*2d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-31"]
(rd/rdate-add (rd/rdate "2d*3") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-10-31"]
```

As with standard arithmetic, the multiplication takes prescedence over addition or subtraction, but can be overruled using brackets.

``` clj
(rd/rdate-add (rd/rdate "3*2d+1d") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-11-01"]
(rd/rdate-add (rd/rdate "3*(2d+1d)") (t/local-date 2017 10 25))
=> #[org.joda.time.LocalDate "2017-11-03"]
```

## License

Released under the MIT License: <https://github.com/InfiniteChai/clj-rdate/blob/master/MIT-LICENSE.txt>
