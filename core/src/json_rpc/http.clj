(ns json-rpc.http
  (:require
   [clj-http.client :as http]
   [json-rpc.client :as client]))

(defn- parse-int
  [s]
  (Integer/parseInt s))

(defn infer-http-status
  "Infer the HTTP status code from the JSON-RPC response.
   See: https://www.jsonrpc.org/historical/json-rpc-over-http.html#errors"
  [body]
  (if-let [error (:error body)]
    (let [code (:code error)]
      (try
        (let [code (parse-int code)]
          (cond
            (= -32600 code)                400
            (= -32601 code)                404
            (#{-32602 -32603 -32700} code) 500
            (<= -32099 -32000)             500
            :else (throw (ex-info "JSON-RPC error code invalid!"
                                  {:response body}))))
        (catch java.lang.NumberFormatException ex
          (throw (ex-info "JSON-RPC error code is not a number!"
                          {:response body} ex)))))))

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
