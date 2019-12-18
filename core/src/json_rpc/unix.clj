(ns json-rpc.unix
  (:require
   [clojure.java.io :as io])
  (:import
   (java.io InputStreamReader PrintWriter)
   (java.nio CharBuffer)
   (java.nio.channels Channels)
   (jnr.unixsocket UnixSocketAddress UnixSocketChannel)))

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
