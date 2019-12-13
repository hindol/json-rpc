(ns json-rpc.core
  (:refer-clojure :exclude [send])
  (:require
   [clojure.core.async :as async :refer [<!!]]
   [clojure.string :as string]
   [clojure.tools.logging :as log]))

(def ^:private ^:const version
  "JSON-RPC protocol version."
  "2.0")

(defn- uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defn encode
  "Encodes JSON-RPC method and params as a valid JSON-RPC request."
  [method params]
  {:jsonrpc version
   :method  method
   :params  params
   :id      (uuid)})

(defn decode
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

(defmulti send!
  "Sends a JSON-RPC call to the server."
  (fn [connection & _]
    (:scheme connection)))

(defmethod send!
  :default
  [& args]
  (let [[connection & _] args]
    (log/warnf "send! called with: %s. No such scheme: %s" args (:scheme connection))))

(defn send!*
  "Like [[send!]] but accepts a variable number of arguments."
  [connection method & params]
  (send! connection method params))
