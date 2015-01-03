(ns edguy.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.json :as middleware]
            [ring.middleware.logger]
            [clojure.tools.logging :as logging]
            [clj-http.client :as http]
            [clojure.string :as str]
            )
  )

(def slack-hook-url (System/getenv "SLACK_HOOK_URL"))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)
   })

(defn post-to-slack [message]
  (logging/info "Sending to Slack!")
  (http/post slack-hook-url {:body (json/generate-string message)}))

(defn pull-request-to-slack-messaage [pr-body]
  {:text (str "<" ((pr-body "user") "html_url") "|" ((pr-body "user") "login") "> has created a new pull request: <" (pr-body "html_url") "|" (pr-body "title") ">")})


(defroutes app-routes
  (GET "/" request 
        (logging/info (str request)))
  (POST "/payload" [pull_request]
        (logging/info (pull_request "title"))
        (logging/info (pull_request "html_url"))
        (logging/info ((pull_request "user") "login"))
        (post-to-slack (pull-request-to-slack-messaage pull_request))
        (json-response {"Title" (pull_request "title")
                        "Url" (pull_request "html_url")
                        "User" ((pull_request "user") "login")
                        "State" (pull_request "state")
                        })
        )
  (POST "/slackbot" {body :body}
        (def slack-data (into {} (map #(array-map (% 0) (% 1 ) ) (map #(str/split % #"=") (str/split (slurp body) #"&")))))
        (logging/info (slack-data "user_name"))
        (logging/info (slack-data "text")))
  (route/not-found "Not Found Sorry"))

(def app
  (->
    app-routes
    middleware/wrap-json-params
    middleware/wrap-json-body
    ring.middleware.logger/wrap-with-logger
    ))
