(defproject edguy "0.1.2-SNAPSHOT"
  :description "An integration bot between Slack and GitHub.  Built purely to help me learn Clojure and to have some fun."
  :url ""
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [ring/ring-json "0.3.1"]
                 [ring.middleware.logger "0.5.0"]
                 [cheshire "5.4.0"]
                 [clj-http "0.9.1"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.xerial/sqlite-jdbc "3.8.7"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler edguy.core.handler/app :port 4567}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
