(ns json-rpc.core
  (:refer-clojure :exclude [send])
  (:require
   [clojure.tools.logging :as log]
   [json-rpc.client :as client]
   [json-rpc.http :as http]
   [json-rpc.json :as json]
   [json-rpc.unix :as unix]
   [json-rpc.url :as url]
   [json-rpc.ws :as ws]))

(def ^:const version
  "JSON-RPC protocol version."
  "2.0")

(defn uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defn encode
  "Encodes JSON-RPC method and params as a valid JSON-RPC request."
  ([method params id]
   (json/write-str json/data-json {:jsonrpc version
                                   :method  method
                                   :params  params
                                   :id      id}))
  ([method params]
   (encode method params (uuid))))

(defn decode
  "Decodes result or error from JSON-RPC response"
  [json]
  (let [body (json/read-str json/data-json json)]
    (select-keys body [:result :error :id])))

(def ^:private ^:const routes
  {:http  http/clj-http
   :https http/clj-http
   :ws    ws/gniazdo
   :wss   ws/gniazdo
   :unix  unix/unix-socket})

(defn router
  [url]
  (let [scheme (url/scheme url)]
    (if-let [client (routes scheme)]
      client
      (throw (ex-info (format "Unsupported scheme: %s. Supported schemes are: %s."
                              (url/scheme url)
                              (keys routes))
                      {:url url})))))

(defn open
  [url]
  (let [client  (router url)
        channel (client/open client url)]
    {:send! (partial client/send! client channel)
     :close (partial client/close client channel)}))

(defn send!
  [{send!-fn :send!} method params & {id :id}]
  (let [id       (or id (uuid))
        request  (encode method params id)
        response (-> request
                     (send!-fn))
        decoded  (decode response)]
    (log/debugf "request => %s, response => %s" request response)
    (if (= id (:id decoded))
      decoded
      (throw (ex-info "Response ID is different from request ID!"
                      {:request  request
                       :response response})))))

(defn close
  [{close-fn :close}]
  (close-fn))
