(ns muzak.core.handler
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources]]
            [chord.http-kit :refer [wrap-websocket-handler]]
            [clojure.core.async :refer [<! >! put! close! go-loop]]
            [hiccup.page :refer [html5 include-js include-css]]
            [cheshire.core :as json]))

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
  ;;(response (json/encode @songs))
  )

(defn create-song [song-name]
  (swap! songs conj song-name)
  ;;(response "") 201
  )

; HDF5 Parsing Experiments
(defn hdf5-do-something []
  (def wconfig (. ch.systemsx.cisd.hdf5.HDF5Factory configure "attribute.h5"))
  (def writer (.writer wconfig))
  (.close writer))

(defn hdf5-get-reader []
  (. ch.systemsx.cisd.hdf5.HDF5Factory openForReading "resources/TRAXLZU12903D05F94.h5"))

(defn hdf5-get-compound [hr] [x]
  {(:attr x) (.getMemberInfo hr (:attr x))})

;builds a map of attribute-value pairs
(defn hdf5-get-attribute [x]
  (def hr (hdf5-get-reader))
  {(:attr x) (.getStringAttribute hr (:path x) (:attr x))})

(defn hdf5-getStringHdf5Reader [hr]
  (.string hr))

(defn hdf5-readStringArray [hr]
  (def sr (hdf5-getStringHdf5Reader hr))
  (.readArray sr "/metadata/similar_artists"))

; NOTE: I believe that the Song_Title is part of a compound data set, needing a compound reader
(defn hdf5-getCompoundHdf5Reader [hr]
  (.compound hr))

;(defn hdf5-get-attr [x]
;  (def hr (hdf5-get-reader))
;  (.getStringAttribute hr (get x :path) (get x :attr)))

;attempt at reading an arbitrary list of tags
;(def x [{:path "/metadata/atrist_terms" :attr "TITLE"}])
(defn get-song-details [x]
  (def hr (hdf5-get-reader))
  (mapcat (partial hdf5-get-compound hr) x))

;test with testParams
(defn get-song [x]
  (mapcat hdf5-get-attribute x))

;use to test get-song (does not work, needs compound reader)
(def testParams [{:path "/metadata/songs" :attr "artist_name"} {:path "/metadata/songs" :attr "title"}])
;use to test get-song (works)
(def testOriginal [{:path "/metadata/artist_terms" :attr "title"}])


;(defn hdf5-getCompoundValue [hr]
;  (def cr (hdf5-getCompoundHdf5Reader hr))
  ; FAIL - We don't know what type of Java object to ask for (ref: http://svncisd.ethz.ch/doc/hdf5/hdf5-13.06/
;  )


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

