(ns json-rpc.http
  (:require
   [clj-http.client :as http]
   [json-rpc.client :as client]))

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
