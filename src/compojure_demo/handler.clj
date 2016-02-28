(ns compojure-demo.handler
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:use ring.util.response)
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.java.jdbc :as sql]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.adapter.jetty :as jetty]))

(def db-config
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/test?useUnicode=true&characterEncoding=UTF-8"
   :user "root"
   :password ""})

(defn pool
  [config]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname config))
               (.setJdbcUrl (str "jdbc:" (:subprotocol config) ":" (:subname config)))
               (.setUser (:user config))
               (.setPassword (:password config))
               (.setMaxPoolSize 1)
               (.setMinPoolSize 1)
               (.setInitialPoolSize 1))]
    {:datasource cpds}))

(def pooled-db (delay (pool db-config)))

(defn db-connection [] @pooled-db)

(defn get-all-documents []
  (response
    (sql/with-connection (db-connection)
      (sql/with-query-results results
        ["select * from document"]
        (into [] results)))))
;  (.println(System/out) "hello world")
;  (def keymap (hash-map :id 1, :title "clojure", :text "clojure is fun"))
;  (println keymap)
;  keymap)

(defn get-document [id]
  (sql/with-connection (db-connection)
    (sql/with-query-results results
      ["select * from document where id = ?" id]
      (cond
        (empty? results) {:status 404}
        :else (response (first results))))))

(defn create-document [doc]
  (println (doc "text"))
    (sql/with-connection (db-connection)
        (sql/insert-record :document {:title (doc "title") :text (doc "text")})))
;    ("insert success"))

(defn update-document [id doc]
  (sql/with-connection (db-connection)
    (let [document (assoc doc "id" id)]
      (sql/update-values :document ["id=?" id] document)))
  (get-document id))

(defn delete-document [id]
  (sql/with-connection (db-connection)
    (sql/delete-rows :document ["id=?" id]))
  {:status 204})

(defroutes app-routes
  (context "/document" []
    (defroutes documents-routes
      (GET  "/" [] (response(get-all-documents)))
      (POST "/" {body :body} (create-document body))
      (context "/:id" [id] (defroutes document-routes
        (GET    "/" [] (get-document id))
        (PUT    "/" {body :body} (update-document id body))
        (DELETE "/" [] (delete-document id))))))
  (route/not-found "Not Found"))

;;(def app
;;  (wrap-defaults app-routes site-defaults))
(def app
  (-> (handler/api app-routes)
    (middleware/wrap-json-body)
    (middleware/wrap-json-response)))

;; start web server
(defn start-server []
  (jetty/run-jetty app-routes {:host "localhost",
                               :port 8000}))

;;(start-server)