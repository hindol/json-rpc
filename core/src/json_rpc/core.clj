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

(def ^:dynamic *numeric-keys*
  "The set of keys whose values are automatically converted from hexadecimal to decimal.")

(def ^:const version
  "JSON-RPC protocol version."
  "2.0")

(defn uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defn encode
  "Encodes JSON-RPC method and params as a valid JSON-RPC request."
  ([method params id]
   (let [request {:jsonrpc version
                  :method  method
                  :params  params
                  :id      id}
         encoded (json/write-str json/data-json request)]
     (log/debugf "map => %s, json => %s" request encoded)
     encoded))
  ([method params]
   (encode method params (uuid))))

(defn decode
  "Decodes result or error from JSON-RPC response"
  [json]
  (let [body (json/read-str json/data-json json)]
    (log/debugf "json => %s, map => %s" json body)
    (select-keys body [:result :error :id])))

(def ^:private ^:const routes
  {:http  http/clj-http
   :https http/clj-http
   :ws    ws/gniazdo
   :wss   ws/gniazdo
   :unix  unix/unix-socket})

(defn route
  [url]
  (let [scheme (url/scheme url)]
    (if-let [client (routes scheme)]
      client
      (throw (ex-info (format "Unsupported scheme: %s. Supported schemes are: %s."
                              (url/scheme url)
                              (keys routes))
                      {:url url})))))

(defrecord Channel [send-fn close-fn]
  java.io.Closeable
  (close [this]
    (close-fn)))

(defn open
  [url & {route-fn :route-fn}]
  (let [route-fn (or route-fn route)
        client   (route-fn url)
        channel  (client/open client url)]
    (log/debugf "url => %s" url)
    (map->Channel {:send-fn  (partial client/send client channel)
                   :close-fn #(client/close client channel)})))

(defn send
  [{send-fn :send-fn} method params & {id :id}]
  (let [id       (or id (uuid))
        request  (encode method params id)
        response (-> request
                     (send-fn))
        decoded  (decode response)]
    (log/debugf "request => %s, response => %s" request response)
    (if (= id (:id decoded))
      decoded
      (throw (ex-info "Response ID is different from request ID!"
                      {:request  request
                       :response response})))))

(defn close
  [{close-fn :close-fn}]
  (close-fn))
