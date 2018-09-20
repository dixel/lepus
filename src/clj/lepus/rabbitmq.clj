(ns lepus.rabbitmq
  (:require [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.exchange :as le]
            [langohr.queue :as lq]
            [langohr.consumers :as lc]
            [langohr.basic :as lb]
            [taoensso.timbre :as log]
            [cheshire.core :as json]))

(defn consume-from-exchange
  [{:keys [host port username password
           exchange routing-key channel]} handler]
  (println "consuming...")
  (let [conn (rmq/connect
              {:host host
               :port port
               :username username
               :password password})
        ch (lch/open conn)
        queue (str "test-data-" routing-key "-" channel)]
    (le/declare ch exchange "topic" {:auto-delete false :durable true})
    (lq/declare ch queue {:exclusive false :auto-delete true})
    (lq/bind ch queue exchange {:routing-key routing-key})
    (lc/subscribe ch
                  queue
                  (fn [_ meta-data payload]
                    (log/debugf "got rabbitmq data: %s/%s" meta-data payload)
                    (handler
                     {:routing-key (:routing-key meta-data)
                      :payload payload}))
                  {:auto-ack true})
    ch))

(defn stop [ch]
  (rmq/close ch))

(defn safe-stop [ch]
  (try
    (stop ch)
    (catch Exception e
      (log/info "not stopping rabbitmq connection: " e))))

