(ns edguy.core.github
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as logging]
            [clj-http.client :as http]))

(def github_user (System/getenv "GITHUB_USERNAME"))
(def github_password (System/getenv "GITHUB_PASSWORD"))
(def github_url (System/getenv "GITHUB_URL"))

(defn parse-pull-request [pr-body]
  {:status (pr-body "state")
   :user ((pr-body "user") "login")
   :user_url ((pr-body "user") "html_url")
   :url (pr-body "html_url")
   :title (pr-body "title")})

(defn query-url [url username password]
   (def response ( http/get url {:basic-auth [username password]}))
   (logging/info response)
   response)

(defn extract-title-and-creator [pull-requests]
  (map parse-pull-request pull-requests))

(defn get-pull-requests [] 
  (sort-by 
    #(% :user) 
    (extract-title-and-creator 
      (json/parse-string
        ((query-url github_url github_user github_password) :body)))))

(defn group-by-user [pull-requests]
	(group-by #(% :user) pull-requests))

(defn pull-requests-by-user [] (group-by-user (get-pull-requests)))


