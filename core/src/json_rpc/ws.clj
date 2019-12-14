(ns json-rpc.ws
  (:require
   [clojure.core.async :as async :refer [<!! >!!]]
   [clojure.data.json :as json]
   [json-rpc.core :as core]
   [gniazdo.core :as ws]))

(defmethod core/connect
  :ws
  [url]
  {:scheme :ws
   :url    url})

(defn- connect
  [url]
  (let [source (async/chan)
        socket (ws/connect url :on-receive #(>!! source %))]
    {:socket socket
     :source source}))

(defprotocol Client
  "A WebSocket client."
  (send! [this connection message] "Writes data into a WebSocket connection."))

; An implementation of [[json-rpc.ws/Client]] that uses `Gniazdo` to send data
; across.
(defrecord GniazdoClient []
  Client
  (send! [this {url :url} message]
    (let [{:keys [socket source]} (connect url)]
      (try
        (ws/send-msg socket message)
        (<!! source)
        (finally (ws/close socket))))))

(def gniazdo
  "An instance of [[GniazdoClient]]."
  (->GniazdoClient))

(defmethod core/send!
  :ws
  [connection method params]
  (future
    (let [{request-id :id
           :as        request}   (core/encode method params)
          {response-id :id
           :as         response} (->> request
                                      (json/write-str)
                                      (send! gniazdo connection)
                                      (json/read-str))]
      (if (not= request-id response-id)
        response
        (throw (ex-info "Response ID did not match request ID!"
                        {:request  request
                         :response response}))))))
