(ns muzak.core.data-stubs
  (:use [hiccup core page]))

;sample code from http://zaiste.net/2014/02/web_applications_in_clojure_all_the_way_with_compojure_and_om/
(defn index-page []
  (html5
    [:head
      [:title "Hello (index-page) World"]
      (include-css "/css/style.css")]
    [:body
      [:h1 "Hello again.."]]))
      
 
;simple clojure function that will return a map that can be response'd to client / json
;takes a (lazy) sequence of inputs and returns a map where each sequence element is counted
(defn assoc-words [counts word]
  (assoc counts word (inc (get counts word 0))))
   
(defn assoc-songs [songs id]
 (assoc songs id {:id id, :title "some song title"}))
 
;Silly Clojure script to create an empty "attribute.h5" file using JHDF5
(defn hdf5-do-something []
  (def wconfig (. ch.systemsx.cisd.hdf5.HDF5Factory configure "attribute.h5"))
  (def writer (.writer wconfig))
  (.close writer))
