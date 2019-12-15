(ns json-rpc.unix
  (:require
   [clojure.java.io :as io]
   [json-rpc.core :as core]
   [json-rpc.json :as json]
   [json-rpc.url :as url])
  (:import
   (java.io InputStreamReader PrintWriter)
   (java.nio CharBuffer)
   (java.nio.channels Channels)
   (jnr.unixsocket UnixSocketAddress UnixSocketChannel)))

(defmethod core/connect
  :unix
  [url]
  {:scheme :unix
   :path   (url/path url)})

(defprotocol Client
  "An UNIX socket client."
  (open [this path] "Opens and returns a UNIX socket for the given path.")
  (write! [this connection message] "Writes text into a UNIX socket.")
  (close [this connection] "Closes the UNIX socket."))

(defrecord UnixClient []
  Client
  
  (open [this path]
    (-> path
        (io/file)
        (UnixSocketAddress.)
        (UnixSocketChannel/open)))
  
  (write! [this channel message]
    (let [buffer (CharBuffer/allocate 1024)]
      (with-open [os     (Channels/newOutputStream channel)
                  writer (PrintWriter. os)
                  is     (Channels/newInputStream channel)
                  reader (InputStreamReader. is)]
        (.write writer message)
        (.read reader buffer)
        (.flip buffer)
        (str buffer)))))

(def unix-client
  "An instance of [[UnixClient]]."
  (->UnixClient))

(defmethod core/send!
  :unix
  [connection method params]
  (future
    (let [{request-id :id
           :as        request}   (core/encode method params)
          {response-id :id
           :as         response} (->> request
                                      (write! unix-client connection)
                                      (json/read-str))]
      (if (not= request-id response-id)
        response
        (throw (ex-info "Response ID did not match request ID!"
                        {:request  request
                         :response response}))))))