(ns json-rpc.http
  (:require
   [clj-http.client :as client]
   [clojure.tools.logging :as log]
   [json-rpc.core :as core]))

(defmethod core/connect
  :http
  [url]
  {:scheme :http
   :url    url})

(defprotocol Client
  "An HTTP client."
  (post! [this url body] "Makes an HTTP POST request."))

; An implementation of [[json-rpc.http/Client]] that uses `clj-http `to make
; the actual requests.
(defrecord CljHttpClient [options]
  Client
  (post! [this url body]
    (future
      (client/post url (merge options {:form-params body})))))

(def clj-http
  "An instance of [[CljHttpClient]] that always talks JSON and does not throw
   on exceptional HTTP status codes."
  (->CljHttpClient {:content-type     :json    ;; Send JSON
                    :as               :json    ;; Receive JSON as Clojure map
                    :coerce           :always  ;; JSONify error responses
                    :throw-exceptions false})) ;; Don't throw on 4XX, 5XX

(defmethod core/send!
  :http
  [{url :url} method params]
  (future
    (let [request  (core/encode method params)
          response @(post! clj-http url request)
          body     (:body response)
          status   (:status response)]
      (log/debugf "request => %s, response => %s" request response)
      {:status status
       :body   (core/decode body)})))
