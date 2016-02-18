(ns compojure-demo.handler
  (:use ring.util.response)
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.adapter.jetty :as jetty]))

(defn get-all-documents []
  (.println(System/out) "hello world")
  (def keymap (hash-map :id 1, :title "clojure", :text "clojure is fun"))
  (println keymap)
  keymap)

(defn get-document [id]
  ("hello world"))
;;  (sql/with-connection (db-connection)
;;    (sql/with-query-results results
;;      ["select * from documents where id = ?" id]
;;      (cond
;;        (empty? results) {:status 404}
;;        :else (response (first results))))))

(defn create-new-document [doc]
  ("hello world"))
;;  (let [id (uuid)]
;;    (sql/with-connection (db-connection)
;;      (let [document (assoc doc "id" id)]
;;        (sql/insert-record :documents document)))
;;    (get-document id)))

(defn update-document [id doc]
  ("hello world"))
;;  (sql/with-connection (db-connection)
;;    (let [document (assoc doc "id" id)]
;;      (sql/update-values :documents ["id=?" id] document)))
;;  (get-document id))

(defn delete-document [id]
  ("hello world"))
;;  (sql/with-connection (db-connection)
;;    (sql/delete-rows :documents ["id=?" id]))
;;  {:status 204})

(defroutes app-routes
  (context "/documents" []
    (defroutes documents-routes
      (GET  "/" [] (response(get-all-documents)))
      (POST "/" {body :body} (create-new-document body))
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