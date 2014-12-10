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

;gets an hdf5 reader
(defn get-reader [f]
  (. ch.systemsx.cisd.hdf5.HDF5Factory openForReading f))

;gets a string reader from an hdf5 reader
(defn getStringHdf5Reader [hr]
  (.string hr))

;gets a compound reader from an hdf5 reader
(defn getCompoundHdf5Reader [hr]
  (.compound hr))

;gets an array of terms (data) from an hdf5 reader
(defn get-terms [hr]
  (def sr (getStringHdf5Reader hr))
  (vec (.readArray sr "/metadata/artist_terms")))

;test with testF
;gets a map of attribute-value pairs (tag-data pairs) from an h5 file
(defn get-song [f]
  (def hr (get-reader f))
  (def cr (getCompoundHdf5Reader hr))
  (def rec (.read cr "/metadata/songs" ch.systemsx.cisd.hdf5.HDF5CompoundDataMap))
  {:artist (get rec "artist_name"),
   :title (get rec "title"),
   :r (* 100 (get rec "song_hotttnesss"))
   :terms (get-terms hr)})

;pass testF to get-song for testing
(def testF "resources/TRAXLZU12903D05F94.h5")

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
  (GET "/hdf5" [] (response (get-song testComp)))
  (resources "/"))

(def app
  #'app-routes)

; HDF5 Parsing Experiments
;(defn hdf5-do-something []
;  (def wconfig (. ch.systemsx.cisd.hdf5.HDF5Factory configure "attribute.h5"))
;  (def writer (.writer wconfig))
;  (.close writer))

;builds a map of attribute-value pairs
;(defn get-song-data [rec, x]
;  {x (get rec x)})

;David_Edit my compiler is yelling at arguments "[hr] [x]" - is this legal or typo?
;(defn hdf5-get-compound [hr, x]
;  {(:attr x) (.getMemberInfo hr (:attr x))})

;(defn hdf5-getStringHdf5Reader [hr]
;  (.string hr))

;(defn hdf5-readStringArray [hr]
;  (def sr (hdf5-getStringHdf5Reader hr))
;  (.readArray sr "/metadata/similar_artists"))

; Read a CompoundDataSet into a Map and return the "title" value
;(defn hdf5-getTitle [cr]
;  (def rec (.read cr "/metadata/songs" ch.systemsx.cisd.hdf5.HDF5CompoundDataMap))
;  (get rec "title"))

; Silly function to delete during last iteration - reads song title with no params
;(defn hdf5-magic []
;  (def hr (hdf5-get-reader))
;  (def cr (hdf5-getCompoundHdf5Reader hr))
;  (hdf5-getTitle cr))

;(defn hdf5-get-attr [x]
;  (def hr (hdf5-get-reader))
;  (.getStringAttribute hr (get x :path) (get x :attr)))

;attempt at reading an arbitrary list of tags
;(def x [{:path "/metadata/atrist_terms" :attr "TITLE"}])
;(defn get-song-details [x]
;  (def hr (hdf5-get-reader))
;  (mapcat (partial hdf5-get-compound hr) x))

