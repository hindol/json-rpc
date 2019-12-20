(ns json-rpc.http
  (:require
   [clj-http.client :as client]))

(defprotocol Client
  "An HTTP client."
  (post! [this url body] "Makes an HTTP POST request."))

; An implementation of [[json-rpc.http/Client]] that uses `clj-http` to make
; the actual requests.
(defrecord CljHttpClient [options]
  Client
  (post! [this url body]
    (->> {:body body}
         (merge options)
         (client/post url)
         :body)))

(def clj-http
  "An instance of [[CljHttpClient]] that does not throw on exceptional
   HTTP status codes."
  (->CljHttpClient {:headers          {"Content-Type" "application/json"
                                       "Accept"       "application/json"}
                    :throw-exceptions false})) ;; Don't throw on 4XX, 5XX
