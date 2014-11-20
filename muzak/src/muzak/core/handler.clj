(ns muzak.core.handler
  (:use muzak.core.data-stubs)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

; parse-int - Clojure code to parse a string as an integer
(defn parse-int [s]
  (Integer. (re-find  #"\d+" s )))

(defroutes app-routes
  (GET "/" [] "Hello World!")
  (GET "/user/:id" [id] (str "<h1>Hello user " (+ 10 (parse-int id)) " (we added 10) </h1>"))
  (GET "/info" {ip :remote-addr} (str "<h1>Hello client IP " ip "  </h1>"))
  (GET "/requestmap" request (str request))

;Some JSON experiments  
  (GET "/widgets" [] (response [{:title "Sweet Emotion", :id "x2342134lkjaslk3"} {:name "Widget 2"}]))
  (GET "/top10" [] (response (reduce assoc-songs {} (range 0 10))))

;Some HDF5 stuff
  (GET "/hdf5-writer" [] (response (hdf5-do-something)))

  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))


;ORIG app definition (before adding JSON responses)
;(def app
;  (wrap-defaults app-routes site-defaults))
