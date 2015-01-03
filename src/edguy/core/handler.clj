(ns edguy.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.json :as middleware]
            [ring.middleware.logger]
            [clojure.tools.logging :as logging]
            [clj-http.client :as http]
            [clojure.string :as str]
            ))

(def slack-hook-url (System/getenv "SLACK_HOOK_URL"))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)
   })

(defn post-to-slack [message]
  (logging/info "Sending to Slack!")
  (http/post slack-hook-url {:body (json/generate-string message)}))

(defn pull-request-to-slack-message [pr-body]
  (def status (pull_request "state"))
  (def user ((pull_request "user") "login"))
  (def user_url ((pull_request "user") "html_url"))
  (def url (pull_request "html_url"))
  (def title (pull_request "title"))
  {:text (str "<" user_url "|" user "> has created a new pull request: <" url "|" title ">")})

(defn replace-+-with-space [text]
  (str/replace text "+" " "))

(def command-map
  { "edguy get my pull requests" ""
    "edguy " ""})

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
                        "State" (pull_request "state")
                        }))
  (POST "/slackbot" {body :body}
        (def slack-data (into {} (map #(array-map (% 0) (% 1 ) ) (map #(str/split % #"=") (str/split (slurp body) #"&")))))
        (logging/debug (slack-data "user_name"))
        (logging/debug (slack-data "text")))
  (route/not-found "Not Found Sorry"))

(def app
  (->
    app-routes
    middleware/wrap-json-params
    middleware/wrap-json-body
    ring.middleware.logger/wrap-with-logger
    ))
