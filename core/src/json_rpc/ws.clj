(ns json-rpc.ws
  (:require
   [clojure.core.async :as async :refer [<!! >!!]]
   [gniazdo.core :as ws]
   [json-rpc.client :as client]))

(defrecord GniazdoClient []
  client/Client
  
  (open [this url]
    (let [source (async/chan)
          socket (ws/connect url :on-receive #(>!! source %))]
      {:socket socket
       :source source}))

  (send [this {:keys [socket source]} message]
    (ws/send-msg socket message)
    (<!! source))

  (close [this {socket :socket}]
    (ws/close socket)))

(def gniazdo
  (->GniazdoClient))
