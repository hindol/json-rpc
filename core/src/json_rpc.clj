(ns json-rpc
  (:refer-clojure :exclude [send])
  (:require
   [clojure.core.async :as async :refer [<! >!]]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [gniazdo.core :as ws]
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

(defn- str->scheme
  "Tries to map input to one of the known schemes."
  [input]
  (condp = input
    "http"  :http
    "https" :https
    "ws"    :ws
    "wss"   :ws
    "unix"  :unix
    (throw (ex-info "No such scheme!"
                    {:scheme input}))))

(defn- scheme
  "Identifies the scheme from an URL."
  [url]
  (let [[scheme _] (string/split url #"://")]
    (str->scheme scheme)))

(defmulti connect
  "Creates a JSON-RPC connection object."
  scheme)

(defmethod connect
  :http
  [url]
  {:scheme :http
   :url    url})

(defmethod connect
  :ws
  [url]
  (let [source (async/chan)
        sink   (async/chan)
        socket (ws/connect url :on-receive #(>! source %))]
    {:scheme :ws
     :socket socket
     :source source
     :sink   sink}))

(defmulti send!
  "Sends a JSON-RPC call to the server."
  (fn [connection & _]
    (:scheme connection)))

(defmethod send!
  :http
  [{url :url} method params]
  (future
    (let [request  (encode method params)
          response @(http/post! http/clj-http url request)
          body     (:body response)
          status   (:status response)]
      (log/debugf "request => %s, response => %s" request response)
      {:status status
       :body   (decode body)})))

(defmethod send!
  :ws
  [{:keys [source sink]} method params]
  (future
    (let [{request-id :id :as request} (encode method params)]
      (>! sink request)
      (let [{response-id :id :as response} (<! source)]
        (if (= request-id response-id)
          response
          (throw (ex-info "Response ID did not match request ID!"
                          {:request  request
                           :response response})))))))

(defmethod send!
  :default
  [& args]
  (let [[connection & _] args]
    (log/warnf "send! called with: %s. No such scheme: %s" args (:scheme connection))))

(defn send!*
  "Like [[send!]] but accepts a variable number of arguments."
  [connection method & params]
  (send! connection method params))
