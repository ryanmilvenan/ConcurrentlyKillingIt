(ns muzak.event-handlers
  (:require [cljs.core.async :refer [put!]]
            [dommy.core :as d]
            [clidget.widget :refer-macros [defwidget]])
  (:require-macros [dommy.macros :refer [node]]))

(defn page-body [!events new-event-ch]
  (node
   [:div{:class "btn-grp", :data-toggle "buttons-checkbox"}
   (bpm-button new-event-ch)
   (filt1-button new-event-ch)
   (filt2-button new-event-ch)]))

(defn example-function [])

(defn bpm-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "BPM"}])
      (with-click-handler new-event-ch)))

(defn filt1-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "FILT1"}])
      (with-click-handler new-event-ch)))

(defn filt2-button [new-event-ch]
  (-> (node [:input {:type "button", :class "btn", :value "FILT2"}])
      (with-click-handler new-event-ch)))

(defn with-click-handler [$button new-event-ch]
  (d/listen! $button :click
    (fn [e]
      (def msg (hash-map :event "parse", :data '(1 2 3)))
      (put! new-event-ch msg))))
