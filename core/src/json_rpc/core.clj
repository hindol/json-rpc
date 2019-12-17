(ns json-rpc.core
  (:refer-clojure :exclude [send])
  (:require
   [clojure.tools.logging :as log]
   [json-rpc.http :as http]
   [json-rpc.json :as json]
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
  "Decodes result or error from JSON-RPC response."
  [json]
  (let [body (json/read-str json/data-json json)]
    (select-keys body [:result :error :id])))

(defmulti connect
  "Creates a JSON-RPC connection object."
  url/scheme)

(defmethod connect
  :http
  [url]
  {:scheme :http
   :url    url})

(defmethod connect
  :https
  [url]
  {:scheme :http
   :url    url})

(defmethod connect
  :ws
  [url]
  {:scheme :ws
   :url    url})

(defmethod connect
  :wss
  [url]
  {:scheme :ws
   :url    url})

(defmethod connect
  :unix
  [url]
  {:scheme :unix
   :path   (url/path url)})

(defmethod connect
  :default
  [url]
  (throw (ex-info (format "Unsupported scheme: %s. Are you passing a valid URL?"
                          (url/scheme url))
                  {:url url})))

(defmulti send!
  "Sends a JSON-RPC call to the server."
  (fn [connection & _]
    (:scheme connection)))

(defmethod send!
  :http
  ([{url :url} client method params]
   (future
     (let [request  (encode method params)
           response (->> request
                         (http/post! client url))]
       (log/debugf "request => %s, response => %s" request response)
       (decode response))))
  ([connection method params]
   (send! connection http/clj-http method params)))

(defmethod send!
  :ws
  ([connection client method params]
   (future
     (let [id       (uuid)
           request  (encode method params id)
           response (->> request
                         (ws/write! client connection)
                         (decode))]
       (if (not= id (:id response))
         response
         (throw (ex-info "Response ID did not match request ID!"
                         {:request  request
                          :response response}))))))
  ([connection method params]
   (send! connection ws/gniazdo method params)))

(defmethod send!
  :default
  [& args]
  (let [[connection & _] args]
    (log/warnf "send! called with: %s. No such scheme: %s."
               args (:scheme connection))))

(defn send!*
  "Like [[send!]] but accepts a variable number of arguments."
  [connection method & params]
  (send! connection method params))
