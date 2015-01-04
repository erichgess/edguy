(ns edguy.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.json :as middleware]
            [ring.middleware.logger]
            [clojure.tools.logging :as logging]
            [clj-http.client :as http]
            [clojure.string :as str])
  (:use [edguy.core.github :as github]))

(def slack-hook-url (System/getenv "SLACK_HOOK_URL"))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})

(defn post-to-slack [message]
  (logging/info "Sending to Slack!")
  (http/post slack-hook-url {:body (json/generate-string message)}))

(defn parse-slack-outgoing-hook [message]
  (into {} (map #(array-map (% 0) (java.net.URLDecoder/decode (% 1))) (map #(str/split % #"=") (str/split (slurp message) #"&")))))

(defn pull-request-to-slack-text [pr-data]
  (str "<" (pr-data :user_url) "|" (pr-data :user) "> has created a new pull request: <" (pr-data :url) "|" (pr-data :title) ">"))

(defn get-pull-requests-for-user [user]
  ((github/pull-requests-by-user) user))

(defn get-all-pull-requests [user]
  (github/get-pull-requests))

(def commands
  {"edguy get my pull requests" get-pull-requests-for-user
   "edguy get all pull requests" get-all-pull-requests})

(defn edguy-routes [user message]
  (logging/info "edguy got " user message)
  ((commands message) ""))

(defroutes app-routes
  (GET "/" request 
        (logging/debug (str request))
        (json-response {"hello" "world"}))
  (POST "/payload" [pull_request]
        (logging/debug (pull_request "title"))
        (logging/debug (pull_request "html_url"))
        (logging/debug ((pull_request "user") "login"))
        (post-to-slack (pull-request-to-slack-message pull_request))
        (json-response {"Title" (pull_request "title")
                        "Url" (pull_request "html_url")
                        "User" ((pull_request "user") "login")
                        "State" (pull_request "state")}))
  (POST "/slackbot" {body :body}
        (def slack-data (parse-slack-outgoing-hook body))
        (logging/info (slack-data "user_name"))
        (logging/info (java.net.URLDecoder/decode (slack-data "text")))
        (logging/info (slack-data "trigger_word"))
        (def x (edguy-routes (slack-data "user_name") (slack-data "text")))
        (json-response {"text" (str/join "\n" (map pull-request-to-slack-text x))}))
  (route/not-found "Not Found Sorry"))

(def app
  (->
    app-routes
    middleware/wrap-json-params
    ring.middleware.logger/wrap-with-logger))
