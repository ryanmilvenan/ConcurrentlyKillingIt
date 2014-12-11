(ns muzak.bubble-chart
  (:require [strokes :refer [d3]]
            [mrhyde.typepatcher :refer [repersist]]
            [clidget.widget :refer-macros [defwidget]])
  (:require-macros [dommy.macros :refer [node sel1]]))

(strokes/bootstrap)

(defwidget bubble-chart [{:keys [msgs]}]
  (def initial "muzak.edn")
  (def diameter 960)
  (def formatfn (-> d3 (.format ",d")))

  (def pack (.. d3 -layout pack
    (size [(- diameter 4), (- diameter 4)])
    (value :size)))

  (def svg (.. d3 (select "#chart-body") (append "svg")
    (attr "width" diameter)
    (attr "height" diameter)
  (append "g")
    (attr "transform" "translate(2,2)")))

  (defn drawBubbles [data]
    (strokes/fetch-edn data (fn [error, root]
      (let [node (.. svg (datum root) (selectAll ".node") (remove)
                    (data (repersist (.-nodes pack) :skip [:children :parent]))
                  (enter) (append "g")
                    (attr "class" #(if (contains? % :children) "node" "leaf node"))
                    (attr "transform" #(str "translate(" (:x %) "," (:y %) ")")))]

      (.. node (append "title")
        (text #(str (:name %) (if (contains? % :children) "" (formatfn (:size %))))))

      (.. node (append "circle")
        (attr "r" :r)
        ;;***********NEEDS THIS PROPERTY(style "fill" "#3182bd")
        ;;*** ALT - if the flare.edn map has a fill property, set directly on svg circle
          (attr "fill" :fill)
          )

      (.. node (filter #(not (:children %))) (append "text")
        (attr "dy" ".3em")
        (style "text-anchor" "middle")
        (text #(subs (:name %) 0 (/ (:r %) 3)))))))


  (.. d3 (select (.-frameElement js/self)) (style "height" (str diameter "px"))))
  (drawBubbles initial)


(comment
  (node
   [:div
    [:h3 "Messages from the server:"]
    [:ul
     (if (seq msgs)
       (for [msg msgs]
         [:li (pr-str msg)])
       [:li "None yet."])]]))
