(ns lepus.routes
  (:require [clojure.java.io :as io]
            [clojure.reflect :as r]
            [compojure.core :refer [ANY GET PUT POST DELETE routes]]
            [compojure.route :refer [resources]]
            [org.httpkit.server :as http]
            [lepus.rabbitmq :as rmq]
            [cheshire.core :as json]
            [taoensso.timbre :as log]
            [ring.util.response :refer [response]]))

(defonce connection (atom {}))

(defn websocket-server [channel rmq-conn config]
  (case (:cmd config)
    "start"
    (do
      (rmq/safe-stop rmq-conn)
      (swap!
       connection
       (fn [state]
         (assoc
          state channel
          (rmq/consume-from-exchange
           (assoc config
                  :channel
                  (.toString channel))
           #(http/send!
             channel
             (json/encode
              (assoc
               (json/decode (String. (:payload %)))
               :routing-key (:routing-key %)))))))))

    "stop" (do
             (rmq/safe-stop rmq-conn)
             (swap! connection
                    #(dissoc % channel))
             (http/send! channel "# stopped"))
    (http/send! channel "# unknown")))

(defn home-routes [endpoint]
  (routes
   (GET "/" _
        (-> "public/index.html"
            io/resource
            io/input-stream
            response
            (assoc :headers {"Content-Type" "text/html; charset=utf-8"})))
   (GET "/ws" req
        (http/with-channel req channel
          (http/on-close channel (fn [status]
                                   (when-let [conn (@connection channel)]
                                     (rmq/safe-stop conn)
                                     (swap! connection
                                            #(dissoc % channel)))
                                   (log/info "channel closed")))
          (http/on-receive channel
                           (fn [data]
                             (let [config (json/decode data true)
                                   rmq-conn (@connection channel)]
                               (websocket-server channel rmq-conn config))))))
   (resources "/")))
