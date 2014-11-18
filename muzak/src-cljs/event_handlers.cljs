(ns muzak.event-handlers
  (:require [cljs.core.async :refer [put!]]
            [dommy.core :as d]
            [clidget.widget :refer-macros [defwidget]])
  (:require-macros [dommy.macros :refer [node]]))

(defn page-body [!events new-event-ch]
  (node
   [:div
   (send-signal-button new-event-ch)]))

(defn send-signal-button [new-event-ch]
  (-> (node [:input {:type "button", :size 50, :value "BPM"}])
      (with-click-handler new-event-ch)))

(defn with-click-handler [$button new-event-ch]
  (d/listen! $button :click
    (fn [e]
      (def msg (hash-map :event "parse", :data '(1 2 3)))
      (put! new-event-ch msg))))
