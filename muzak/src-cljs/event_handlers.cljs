(ns muzak.event-handlers
  (:require [cljs.core.async :refer [put!]]
            [dommy.core :as d]
            [clidget.widget :refer-macros [defwidget]])
  (:require-macros [dommy.macros :refer [node]]))

(defn page-body [!events new-event-ch]
  (node
    [:div
      [:h1#million "Million Song Search"]
      [:p "Select Filters:"]
      [:div{:class "wrapper text-center"}
        [:div{:class "btn-grp", :data-toggle "buttons-checkbox"}
         (bpm-button new-event-ch)
         (filt1-button new-event-ch)
         (filt2-button new-event-ch)
         (pop-button new-event-ch)]]
      [:div#bubble-chart
        [:h1 "testing"]]
      [:p "Sort By:"]
      [:div{:class "wrapper text-center"}
        [:div{:class "btn-grp", :data-toggle "buttons-checkbox"}
         (popularity-button new-event-ch)
         (hottness-button new-event-ch)
         (tempo-button new-event-ch)
         (dance-ability-button new-event-ch)]]]))

(def search-object {:POP false, :CLASSICAL false, :ROCK false, :GENRE false})

(defn bpm-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "BPM"}])
      (with-click-handler new-event-ch "BPM")))

(defn filt1-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "FILT1"}])
      (with-click-handler new-event-ch "FILT1")))

(defn filt2-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "FILT2"}])
      (with-click-handler new-event-ch "FILT2")))

(defn pop-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "Pop"}])
      (with-click-handler new-event-ch "Pop")))

(defn popularity-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "Popularity"}])
      (with-click-handler new-event-ch "Popularity")))

(defn hottness-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "Hottness"}])
      (with-click-handler new-event-ch "Hottness")))

(defn tempo-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "Tempo"}])
      (with-click-handler new-event-ch "Tempo")))

(defn dance-ability-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "Dance-ability"}])
      (with-click-handler new-event-ch "Dance-ability")))

(defn with-click-handler [$button new-event-ch name]
  (d/listen! $button :click
    (fn [e]
      (def msg (hash-map :event "parse", :data name))
      (put! new-event-ch msg))))
