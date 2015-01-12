(ns edguy.core.users
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as logging]
            [clojure.java.jdbc :as jdbc]))

(defn update-or-insert!
  "Updates columns or inserts a new row in thie specified table"
  [db table row where-clause]
    (jdbc/with-db-transaction [t-con db]
      (let [result (jdbc/update! t-con table row where-clause)]
        (if (zero? (first result))
        (jdbc/insert! t-con table row)
        result))))

(def db {:subprotocol "sqlite"
                :subname "../data/edguy.db"
                :classname "org.sqlite.JDBC"})

(defn create-users-table []
  (try (jdbc/db-do-commands db
    (jdbc/create-table-ddl :users 
      [:slack_id :varchar "NOT NULL" "PRIMARY KEY"]
      [:github_id :varchar]
      [:wrike_id :varchar]))))

(defn create-database []
  (create-users-table))

(defn get-user-accounts [slack_id]
  (vec (try (jdbc/query db
          ["SELECT github_id,wrike_id FROM users WHERE slack_id=?" slack_id]))))

(defn add-user [slack_id]
  (try (update-or-insert! db :users {:slack_id slack_id} ["slack_id=?" slack_id])))

(defn set-github-account [slack_id github_id]
  (try (update-or-insert! db :users {:slack_id slack_id :github_id github_id} ["slack_id=?" slack_id])))
