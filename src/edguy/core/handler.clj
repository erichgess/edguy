(ns edguy.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.json :as middleware]
            [ring.middleware.logger]
            [clojure.tools.logging :as logging]
            [clj-http.client :as http]
            [clojure.string :as str])
  (:use [edguy.core.github :as github]
        [edguy.core.users :as users]))

(def slack-hook-url (System/getenv "EDGUY_SLACK_HOOK_URL"))

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
  (logging/info pr-data)
  (if pr-data
    (str "<" (pr-data :user_url) "|" (pr-data :user) "> has created a new pull request: <" (pr-data :url) "|" (pr-data :title) ">")
    "No Pull Requests"))

(defn multiple-pull-requests-to-slack [prs]
  (str/join "\n" (map pull-request-to-slack-text prs)))

(defn get-pull-requests-for-user [user params]
  (logging/info "getting pull requests for user %s" user)
  (-> ((github/pull-requests-by-user) user)
      (pull-request-to-slack-text)))

(defn set-users-github-account [user params]
  (logging/info (str params))
  (users/set-github-account user (params 0))
  (format "@%s mapped to GitHub account %s" user (params 0)))

(defn get-all-pull-requests-command [user params]
  (-> (github/get-pull-requests) multiple-pull-requests-to-slack))

(def command-to-function
  [[#"edguy get my pull requests" get-pull-requests-for-user]
   [#"edguy get all pull requests" get-all-pull-requests-command]
   [#"edguy my github account is (.*)" set-users-github-account]])

(defn parse-message [command-patterns message]
  (some #(let [regex (re-find (% 0) message)] (if regex [(vec (rest regex)) (% 1)])) command-patterns))

(defn edguy-routes [user message]
  (logging/info "edguy got " user message)
  (def accounts (users/get-user-accounts user))
  (logging/info "accounts " (str accounts))
  (let [cmd (parse-message command-to-function  message)] ((cmd 1) user (cmd 0))))

(defroutes app-routes
  (GET "/" request 
        (logging/debug (str request))
        (json-response {"hello" "world"}))
  (POST "/payload" [pull_request]
        (logging/debug (pull_request "title"))
        (logging/debug (pull_request "html_url"))
        (logging/debug ((pull_request "user") "login"))
        (post-to-slack (pull-request-to-slack-text pull_request))
        (json-response {"Title" (pull_request "title")
                        "Url" (pull_request "html_url")
                        "User" ((pull_request "user") "login")
                        "State" (pull_request "state")}))
  (POST "/slackbot" {body :body}
        (def slack-data (parse-slack-outgoing-hook body))
        (logging/info (slack-data "user_name"))
        (logging/info (slack-data "text"))
        (logging/info (slack-data "trigger_word"))
        (def x (edguy-routes (slack-data "user_name") (slack-data "text")))
        (logging/info "Sending" x)
        (json-response {"text" x}))
  (route/not-found "Not Found Sorry"))

(def app
  (->
    app-routes
    middleware/wrap-json-params
    ring.middleware.logger/wrap-with-logger))
