(ns muzak.core.handler
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources]]
            [chord.http-kit :refer [wrap-websocket-handler]]
            [clojure.core.async :refer [<! >! put! close! go-loop]]
            [hiccup.page :refer [html5 include-js include-css]]))

(defn page-frame []
  (html5
   [:head
    [:title "Muzak"]
    (include-js "/js/jquery-2.1.1.min.js")
    (include-js "/js/bootstrap.min.js")
    (include-js "/js/d3.min.js")
    (include-js "/js/main.js")
    (include-css "/css/bootstrap.min.css")
    (include-css "/css/style.css")]
   [:body [:div#content][:h1 "Hello"]]))


;; Event Handler Functions
(defn parse-event-handler [data]
  (prn data))

(def songs (atom ()))

(defn list-songs []
  (response (json/encode @songs)))

(defn create-song [song-name]
  (swap! songs conj song-name)
  (response "") 201)

(defn hdf5-do-something []
  (def wconfig (. ch.systemsx.cisd.hdf5.HDF5Factory configure "attribute.h5"))
  (def writer (.writer wconfig))
  (.close writer))

;; Event listeners
(defn ws-handler [{:keys [ws-channel] :as req}]
  (println "Opened connection from" (:remote-addr req))
  (go-loop []
    (when-let [{:keys [message error] :as msg} (<! ws-channel)]

      ;; Retrieve event field of message
      (def event (get (get msg :message) :event))

      ;; Retrieve data field of message
      (def data (get (get msg :message) :data))

      ;; Perform callback associated with the event
      (cond
       (= event "parse") (parse-event-handler data)
       :else (prn "No event handler found"))

      (>! ws-channel (if error
                       (format "Error: '%s'." (pr-str msg))
                       {:received (format "You passed: '%s' at %s." (pr-str message) (java.util.Date.))}))
      (recur))))


(defroutes app-routes
  (GET "/" [] (response (page-frame)))
  (GET "/ws" [] (-> ws-handler
                    (wrap-websocket-handler {:format :json-kw})))
  (resources "/"))

(def app
  #'app-routes)

