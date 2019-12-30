(ns json-rpc.http
  (:require
   [clj-http.client :as http]
   [json-rpc.client :as client]))

(defn error->status
  "Converts a JSON-RPC error code into the appropriate HTTP status."
  [code]
  (cond
    (= -32600 code)                400
    (= -32601 code)                404
    (#{-32602 -32603 -32700} code) 500
    (<= -32099 code -32000)        500))

(defn infer-status
  "Infer the HTTP status code from the JSON-RPC response.
   See: https://www.jsonrpc.org/historical/json-rpc-over-http.html#errors"
  [body]
  (if-let [error (:error body)]
    (if-let [code (:code error)]
      (if (int? code)
        (if-let [status (error->status code)]
          status
          (throw (ex-info "Error code invalid!"
                          {:response body})))
        (throw (ex-info "Error code is not a number!"
                        {:response body})))
      (throw (ex-info "Error code missing from error response!"
                      {:response body})))
    200))

(defrecord CljHttpClient [options]
  client/Client

  (open [this url]
    {:url url})

  (send! [this {url :url} message]
    (->> {:body message}
         (merge options)
         (http/post url)
         :body))

  (close [this conneciton]
    ;; No-op
    ))

(def clj-http
  (->CljHttpClient {:headers          {"Content-Type" "application/json"
                                       "Accept"       "application/json"}
                    :throw-exceptions false})) ;; Don't throw on 4XX, 5XX
