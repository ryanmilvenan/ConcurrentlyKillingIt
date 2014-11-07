(ns muzak.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

; parse-int - Clojure code to parse a string as an integer
(defn parse-int [s]
  (Integer. (re-find  #"\d+" s )))

(defroutes app-routes
  (GET "/" [] "Hello World!")
  (GET "/user/:id" [id] (str "<h1>Hello user " (+ 10 (parse-int id)) " (we added 10) </h1>"))
  (GET "/info" {ip :remote-addr} (str "<h1>Hello client IP " ip "  </h1>"))
  (GET "/requestmap" request (str request))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
