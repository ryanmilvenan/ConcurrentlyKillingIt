(ns muzak.core.handler
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources]]
            [chord.http-kit :refer [wrap-websocket-handler]]
            [clojure.core.async :refer [<! >! >!! put! close! go-loop]]
            [hiccup.page :refer [html5 include-js include-css]]
            [cheshire.core :as json]
            [muzak.core.hdf5-parse :refer [magic-write-edn]]))


;; Search maps
(def search-maps-by-addr (hash-map))
(def default-map {:BPM false, :POP false, :CLASSICAL false, :ROCK false})
(defn initialize-state [ws-channel addr]
  (def search-maps-by-addr (assoc search-maps-by-addr addr default-map))
  (>!! ws-channel (hash-map :event "state" :data (get search-maps-by-addr addr))))


(defn update-search-map [addr new-state]
  (def search-maps-by-addr (assoc search-maps-by-addr addr new-state)))

;; Page Information
(defn page-frame []
  (html5
   [:head
    [:title "Muzak"]
    (include-js "/js/jquery-2.1.1.min.js")
    (include-js "/js/bootstrap.min.js")
    (include-js "/js/d3.min.js")
    (include-js "/js/main.js")
    (include-css "/css/bootstrap.min.css")
    (include-css "http://fonts.googleapis.com/css?family=Lobster")
    (include-css "/css/style.css")]
   [:body [:div#content]]))



;; Event Handler Functions
(defn parse-event-handler [ws-channel addr data error]
  (update-search-map addr data)
  ;; NEED Mangle client request into single like "rock"
  (def keywords (clojure.string/lower-case (apply name (filter (merge-with not= data default-map) (keys data)))))
  ;;    TODO - Should be easy to extend HDF5 filter to work with collection of string terms
  ;; something like ("rock" "pop" "metal")
  (magic-write-edn keywords)
  (>!! ws-channel (if error
                    (format "Error: '%s'." (pr-str data))
                    (hash-map :event "result" :data data))))


;; Event listeners
(defn ws-handler [{:keys [ws-channel] :as req}]
  (println "Opened connection from" (:remote-addr req))
  (initialize-state ws-channel (:remote-addr req))
  (go-loop []
      (when-let [{:keys [message error] :as msg} (<! ws-channel)]

          (let
            [;; Retrieve address of requester
             addr (:remote-addr req)

             ;; Retrieve event field of message
             event (get-in msg [:message :event])

             ;; Retrieve data field of message
             data (get-in msg [:message :data])]
          (prn data)
          ;; Perform callback associated with the event
          (cond
           (= event "parse") (parse-event-handler ws-channel addr data error)
           :else (prn "No event handler found"))))

      (recur)))

(defroutes app-routes
  (GET "/" [] (response (page-frame)))
  (GET "/ws" [] (-> ws-handler
                    (wrap-websocket-handler {:format :json-kw})))
  (GET "/rebuild-edn" []
       (magic-write-edn "rock")
       ;(response "now refresh main page"))
       (response (page-frame)))

  (resources "/"))

(def app
  #'app-routes)


