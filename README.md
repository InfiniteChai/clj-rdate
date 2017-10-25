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

## License

Released under the MIT License: <https://github.com/InfiniteChai/clj-rdate/blob/master/MIT-LICENSE.txt>
