(ns json-rpc.pedestal
  (:require
   [io.pedestal.interceptor :as intc]
   [json-rpc]))

(def json-rpc
  "A Pedestal interceptor capable of making JSON-RPC requests."
  (intc/interceptor
   {:name ::json-rpc
    :enter (fn [context]
             (let [request    (:request context)
                   connection (:json-rpc-connection request)
                   method     (:json-rpc-method request)
                   params     (:json-rpc-params request)
                   response   (json-rpc/send! connection method params)]
               (assoc context :response response)))}))
