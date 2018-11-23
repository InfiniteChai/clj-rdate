(defproject clj-rdate "0.1.0"
  :description "A relative date library for Clojure"
  :url "https://github.com/InfiniteChai/clj-rdate"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :deploy-repositories [
    ["clojars"  {:sign-releases false :url "https://clojars.org"}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-time "0.14.0"]
                 [instaparse "1.4.8"]]
  :profiles {
    :dev {
      :dependencies [[proto-repl "0.3.1"]]
    }
    })
