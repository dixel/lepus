(ns lepus.core
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.string :as gstring]
            [goog.string.format]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(enable-console-print!)

(defonce app-state
  (atom {:left nil
         :config {}
         :middle '()
         :connected false
         :ws nil}))

(defn ->json [data]
  (.stringify
   js/JSON
   (clj->js
    data)))

(defn config-component
  "defines config input"
  [name & {:keys [transform-fn
                  input-type]
           :or {transform-fn identity
                input-type "text"}}]
  [:div
   [:input
    {:id name
     :on-key-up #(swap!
                  app-state
                  (fn [state]
                    (let [component-value
                          (.-value (js/document.getElementById name))]
                      (-> state
                          (assoc-in
                           [:config (keyword name)]
                           (transform-fn component-value))))))
     :placeholder name
     :type input-type}]])

(def playback-component
  [:div
   [:button
    {:on-click
     (fn []
       (try
         (.close (:ws @app-state))
         (catch js/Object e
           (.log js/console "failed to close websocket...")))
       (let [ws (:ws
                 (swap!
                  app-state
                  #(assoc
                    % :ws
                    (js/WebSocket.
                     (str "ws://" (.-host js/location) "/ws")))))]
         (set! (.-onmessage ws)
               (fn [msg]
                 (swap! app-state
                        (fn [state]
                          (update state :middle
                                  (fn [middle]
                                    (conj middle
                                          (->> (.-data msg)
                                               (.parse js/JSON)
                                               (#(.stringify js/JSON
                                                             %
                                                             nil
                                                             2))))))))))
         (set! (.-onopen ws)
               (fn []
                 (.log js/console "websocket opened, sending payload")
                 (.log js/console (.send ws (->json
                                             (assoc (:config @app-state)
                                                    :cmd :start))))))))}
    [:i {:class "fas fa-play"}]]
   [:button
    {:on-click (fn []
                 (when-let [ws (:ws @app-state)]
                   (.send ws (->json {:cmd :stop}))))}
    [:i {:class "fas fa-pause"}]]
   [:button
    {:on-click (fn []
                 (swap! app-state #(assoc % :middle '()))
                 (when-let [ws (:ws @app-state)]
                   (.send ws (->json {:cmd :stop}))))}
    [:i {:class "fas fa-stop"}]]])

(defn app []
  [:div {:id "wrap"}
   [:div {:id "left"}
    [:p {:id "logo"} [:b {:id "logo1"} ">_"] "lepus"]
    (config-component "host")
    (config-component "port"
                      :transform-fn #(js/parseInt %))
    (config-component "username")
    (config-component "password"
                      :input-type "password")
    (config-component "exchange")
    (config-component "routing-key")
    playback-component]
   [:div {:id "middle"}
    (for [item (:middle @app-state)]
      ^{:key (.random js/Math)}
      [:div
       [:pre item]
       [:hr]])]])

(defn render []
  (reagent/render [app] (js/document.getElementById "app")))
