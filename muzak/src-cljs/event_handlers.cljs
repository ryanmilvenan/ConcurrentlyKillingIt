(ns muzak.event-handlers
  (:require [cljs.core.async :refer [put!]]
            [dommy.core :as d]
            [clidget.widget :refer-macros [defwidget]])
  (:require-macros [dommy.macros :refer [node]]))

(enable-console-print!)


;; Toggle Handler

(def search-map {})

(defn toggle-value [hash-key]
  (let [value (get search-map hash-key)]
    (let [new-value (not value)]
      (def search-map (assoc search-map hash-key new-value)))))


;; Click Handlers

(defn with-click-handler-toggle-map [$button hash-key]
  (d/listen! $button :click
    (fn [e]
      (toggle-value hash-key)
      (println search-map))))

;;(defn with-click-handler-toggle-sort [$li

(defn with-click-handler-search [$button new-event-ch]
  (d/listen! $button :click
    (fn [e]
      (let [msg (hash-map :event "parse", :data search-map)]
      (put! new-event-ch msg)))))


;; Toggle Buttons

(defn bpm-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn filter-button", :value "BPM"}])
      (with-click-handler-toggle-map :BPM)))

(defn classical-button [new-event-ch]
  (toggle-value :CLASSICAL)
  (-> (node [:input {:type "button", :class "btn filter-button", :value "Classical"}])
      (with-click-handler-toggle-map :CLASSICAL)))

(defn rock-button [new-event-ch]
  (toggle-value :ROCK)
  (-> (node [:input {:type "button", :class "btn filter-button", :value "Rock"}])
      (with-click-handler-toggle-map :ROCK)))

(defn pop-button [new-event-ch]
  (toggle-value :POP)
  (-> (node [:input {:type "button", :class "btn filter-button", :value "Pop"}])
      (with-click-handler-toggle-map :POP)))

(defn popularity-button [new-event-ch]
  (-> (node [:li[:a {:class "active", :html "Popularity"}]])
      (with-click-handler-toggle-map :POPULARITY)))

(defn hottness-button [new-event-ch]
  (-> (node [:li[:a {:class "btn", :html "Hottness"}]])
      (with-click-handler-toggle-map :HOTNESS)))

(defn tempo-button [new-event-ch]
  (-> (node [:li[:a {:class "btn", :html "Tempo"}]])
      (with-click-handler-toggle-map :TEMPO)))

(defn dance-ability-button [new-event-ch]
  (-> (node [:li[:a {:class "btn", :html "Dance-ability"}]])
      (with-click-handler-toggle-map :DANCE)))


;; Search Button

(defn search-button [new-event-ch]
  (-> (node [:button {:type: "button", :class "btn btn-warning", :id "search-button"}
               [:i {:class "icon-search", :aria-hidden "true"}]])
      (with-click-handler-search new-event-ch)))

(defn update-state [new-state]
  (def search-map new-state))


;; Page Body

(defn page-body [new-event-ch]
  (node
    [:div
      [:h1#million "Million Song Search"]
      [:p "Select Filters:"]
      [:div
        [:div{:class "btn-grp",:id "filters", :data-toggle "buttons-checkbox"}
         (bpm-button new-event-ch)
         (classical-button new-event-ch)
         (rock-button new-event-ch)
         (pop-button new-event-ch)]
       [:div{:id "search-button-container"}
        (search-button new-event-ch)]
       ]
      [:div#bubble-chart
        [:div#chart-body]]
      [:p "Sort By:"]
        [:div
         [:ul{:class "nav nav-pills"}
           (popularity-button new-event-ch)
           (hottness-button new-event-ch)
           (tempo-button new-event-ch)
           (dance-ability-button new-event-ch)]]]))

(defwidget bubble-chart [{:keys [msgs]}]
  (node
   [:div
    [:h3 "Messages from the server:"]
    [:ul
     (if (seq msgs)
       (for [msg msgs]
         [:li (pr-str msg)])
       [:li "None yet."])]]))

(defn page-component [!events new-event-ch]
  (node
   [:div
    (page-body new-event-ch)
    (bubble-chart {:!msgs !events})]))











