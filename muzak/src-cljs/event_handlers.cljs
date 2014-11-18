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
  (-> (node [:input {:type "button", :size 50, :value "Click Me"}])
      (with-click-handler new-event-ch)))

(defn with-click-handler [$button new-event-ch]
  (d/listen! $button :click
    (fn [e]
      (put! new-event-ch "EVENT HEARD!"))))
