(ns muzak.front-end
  (:require [chord.client :refer [ws-ch]]
            [muzak.event-handlers :refer [page-component update-state]]
            [muzak.bubble-chart :refer [bubble-chart]]
            [cljs.core.async :refer [chan <! >! put! close! timeout]]
            [dommy.core :as d]
            [cljs.reader :as edn]
            [clidget.widget :refer-macros [defwidget drawBubbles]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [dommy.macros :refer [node sel1]]))

(enable-console-print!)

(defn add-msg [msgs new-msg]
  ;; we keep the most recent 10 messages
  (->> (cons new-msg msgs)
       (take 10)))

(defn receive-msgs! [!msgs server-ch]
  ;; every time we get a message from the server, add it to our list
  (go-loop []
    (when-let [msg (<! server-ch)]
      (let [event (get-in msg [:message :event])
            data (get-in msg [:message :data])]
        (cond
         (= event "state") (update-state data)
         (= event "result") (
                             swap! !msgs add-msg msg
                             ;NEED TO REDRAW server-updated "muzak.edn"
                             ;(drawBubbles "muzak.edn")
                             )
         :else (swap! !msgs add-msg msg))
      )
      (recur))))

(defn send-msgs! [new-msg-ch server-ch]
  ;; send all the messages to the server
  (go-loop []
    (when-let [msg (<! new-msg-ch)]
      (>! server-ch msg)
      (recur))))

(set! (.-onload js/window)
      (fn []
        (go
          (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:3000/ws"
                                                      {:format :json-kw}))]

            (if error
              ;; connection failed, print error
              (d/replace-contents! (sel1 :#content)
                                   (node
                                    [:div
                                     "Couldn't connect to websocket: "
                                     (pr-str error)]))

              (let [;; !msgs is a shared atom between the model (above,
                    ;; handling the WS connection) and the view
                    ;; (message-component, handling how it's rendered)
                    !msgs (doto (atom [])
                            (receive-msgs! ws-channel))

                    ;; new-msg-ch is the feedback loop from the view -
                    ;; any messages that the view puts on here are to
                    ;; be sent to the server
                    new-msg-ch (doto (chan)
                                 (send-msgs! ws-channel))]

                ;; show the message component
                (d/replace-contents! (sel1 :#content) (page-component !msgs new-msg-ch))
                (bubble-chart {:!msgs !msgs})
                ))))))


