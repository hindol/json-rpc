(ns json-rpc
  (:refer-clojure :exclude [send])
  (:require
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [json-rpc.http :as http]))

(def ^:private ^:const version
  "JSON-RPC protocol version."
  "2.0")

(defn- uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defn- encode
  "Encodes JSON-RPC method and params as a valid JSON-RPC request."
  [method params]
  {:jsonrpc version
   :method  method
   :params  params
   :id      (uuid)})

(defn- decode
  "Decodes result or error from JSON-RPC response."
  [body]
  (select-keys body [:result :error]))

(defn connect
  "Creates a JSON-RPC connection object."
  [url]
  (let [[scheme path] (string/split url #"://" 2)]
    {:scheme (keyword scheme)
     :path   path
     :url    url}))

(defmulti send!
  "Sends a JSON-RPC call to the server."
  (fn [connection & _]
    (:scheme connection)))

(defmethod send!
  :http
  [connection method params]
  (future
    (let [url      (:url connection)
          request  (encode method params)
          response @(http/post! http/clj-http url request)
          body     (:body response)
          status   (:status response)]
      (log/debugf "request => %s, response => %s, status => %s" request body status)
      {:status status
       :body   (decode body)})))

(defmethod send!
  :default
  [& args]
  (let [[connection & _] args]
    (log/warnf "send! called with: %s. No such scheme: %s" args (:scheme connection))))

(defn send!*
  "Like [[send!]] but accepts a variable number of arguments."
  [connection method & params]
  (send! connection method params))
