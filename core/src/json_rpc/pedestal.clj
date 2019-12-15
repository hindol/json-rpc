(ns json-rpc.pedestal
  (:require
   [clojure.core.async :as async]
   [io.pedestal.interceptor :as intc]
   [json-rpc.core :as core]))

(defn- parse-int
  [s]
  (Integer/parseInt s))

(defn- infer-http-status
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

(def json-rpc
  "A Pedestal interceptor capable of making JSON-RPC requests."
  (intc/interceptor
   {:name ::json-rpc
    :enter (fn [context]
             (async/go
               (let [request    (:request context)
                     connection (:json-rpc-connection request)
                     method     (:json-rpc-method request)
                     params     (:json-rpc-params request)
                     body       @(core/send! connection method params)]
                 (assoc context :response {:status (infer-http-status body)
                                           :body   body}))))}))
