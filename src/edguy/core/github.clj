(ns edguy.core.github
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as logging]
            [clj-http.client :as http]))

(def github_user (System/getenv "GITHUB_USERNAME"))
(def github_password (System/getenv "GITHUB_PASSWORD"))
(def github_url (System/getenv "GITHUB_URL"))

(defn query-url [url username password]
   ( http/get url {:basic-auth [username password]}))

(defn extract-title-and-creator [pull-requests]
  (map #(array-map 
		:title (% "title") 
		:user ((% "user") "login")
                :user_url ((% "user") "html_url") 
        	:url (((% "_links") "html") "href"))
          pull-requests))

(defn get-pull-requests (sort-by #(% :user) (extract-title-and-creator
               (chess/parse-string
                 ((query-url github_url github_user github_password) :body)))))

(defn group-by-user [pull-requests]
	(group-by #(% :user) pull-requests))

(def pull-requests-by-user (group-by-user (get-pull-requests)))


