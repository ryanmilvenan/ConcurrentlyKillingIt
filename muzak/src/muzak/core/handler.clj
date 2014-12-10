(ns muzak.core.handler
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources]]
            [chord.http-kit :refer [wrap-websocket-handler]]
            [clojure.core.async :refer [<! >! >!! put! close! go-loop]]
            [hiccup.page :refer [html5 include-js include-css]]
            [cheshire.core :as json]))


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


;; BEGIN ----- HDF5 Parsing ------

; create a java.io.file from the MillionSongSubset folder, return lazy seq of all files
(defn get-h5-files []
  (def f (clojure.java.io/file "resources/MillionSongSubset/data"))
  (defn is-not-dir [f] (not (.isDirectory f))) ; simple predicate

  ;(seq (.listFiles f)) ;Java.io.file has a "listFiles" method
  (filter is-not-dir (file-seq f)))

; wasn't sure how to pass the ".getPath" to map
(defn get-path [f] (.getPath f))

; Java Interop -call HDF5Factory to retrieve a HDF5Reader instance
(defn get-h5-reader [path]
  (. ch.systemsx.cisd.hdf5.HDF5Factory openForReading path))

; NOTE: I believe that the Song_Title is part of a compound data set, needing a compound reader
(defn hdf5-getCompoundHdf5Reader [hr]
  (.compound hr))

; color generator
(def dot-colors ["blue" "green" "yellow" "purple" "red" "cyan"])

(let [number-colors (count dot-colors)]
  (defn rand-color []
    (get dot-colors (rand-int number-colors))))


;Get the "title" field from the Compound DataSet "/metadata/songs" for given relative path
(defn get-song [path]
  (def cr (hdf5-getCompoundHdf5Reader (get-h5-reader path)))
  (def rec (.read cr "/metadata/songs" ch.systemsx.cisd.hdf5.HDF5CompoundDataMap))
  (let [bubble_size 100] ; default mulitplier

    {:name (get rec "title"), :artist (get rec "artist_name"), :album (get rec "release"),
       :id (get rec "song_id")

       :fill (rand-color),

       :size (* bubble_size (get rec "song_hotttnesss")) ;default bubble size field (client can choose other field)

       :artist_hottness (* bubble_size (get rec "artist_hotttnesss"))
       :song_hottness (* bubble_size (get rec "song_hotttnesss"))
       :artist_familiarity (* bubble_size (get rec "artist_familiarity"))
       :artist_latitude (get rec "artist_latitude")
       :artist_longitude (get rec "artist_longitude")
     }))

; ex: (write-edn "resources/public/muzak.edn")
(defn write-edn [obj f-out]
  (spit f-out (pr-str obj)))

;Magic (since it doesn't take any client search params) - overwrites resources/public/muzak.edn
(defn magic-build-edn []
  (def paths (map get-path (take 100 (get-h5-files))))

  ;NOTE: JHDF5 API says we are supposed to close the readers - we are not (MEM-LEAK ??)

  {:name "all" :r 100 :fill "#3182bd" ;some defaults for parent bubble/circle
     :children (mapv get-song paths)})

;Magic since it doesn't take client search request OR output destination to overwrite
(defn magic-write-edn []
  (write-edn (magic-build-edn) "resources/public/muzak.edn"))
;; END    ----- HDF5 Parsing ------


;; Event Handler Functions
(defn parse-event-handler [ws-channel addr data error]
  (update-search-map addr data)
  (>!! ws-channel (if error
                    (format "Error: '%s'." (pr-str data))
                    (hash-map :event "result" :data data))))


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

;returns true if song:terms contains term, otherwise returns false
(defn check-song [term, song]
  (some? (some #{term} (get song :terms))))

;returns a list of maps that is a subset of songs where :terms contains term
(defn filter-songs [term, songs]
  (filter (partial check-song term) songs))

;tests for filter-songs. "pop" returns 1 result, "metal" returns 0 results
;(def s (get-song testF))
;(filter-songs "pop" [s])
;(filter-songs "metal" [s])

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
       (magic-write-edn)
       (response "now refresh main page"))
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

