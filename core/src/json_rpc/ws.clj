(ns json-rpc.ws
  (:require
   [clojure.core.async :as async :refer [<!! >!!]]
   [clojure.data.json :as json]
   [json-rpc.core :as core]
   [gniazdo.core :as ws]))

(defmethod core/connect
  :ws
  [url]
  (let [source (async/chan)
        socket (ws/connect url :on-receive #(>!! source %))]
    {:scheme :ws
     :socket socket
     :source source}))

(defprotocol Client
  "A WebSocket client."
  (send! [this connection ]))

(defmethod core/send!
  :ws
  [{:keys [socket source]} method params]
  (future
    (let [{request-id :id
           :as        request} (core/encode method params)]
      (ws/send-msg socket (json/write-str request))
      (let [{response-id :id :as response} (json/read-str (<!! source))]
        (if (not= request-id response-id)
          response
          (throw (ex-info "Response ID did not match request ID!"
                          {:request  request
                           :response response})))))))
