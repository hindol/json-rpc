(ns json-rpc.unix
  (:require
   [clojure.java.io :as io]
   [json-rpc.client :as client])
  (:import
   (java.io InputStreamReader PrintWriter)
   (java.nio CharBuffer)
   (java.nio.channels Channels)
   (jnr.unixsocket UnixSocketAddress UnixSocketChannel)))

(defrecord UnixSocketClient []
  client/Client

  (open [this path _headers]
    (-> path
        (io/file)
        (UnixSocketAddress.)
        (UnixSocketChannel/open)))

  (send [this channel message]
    (let [buffer (CharBuffer/allocate 1024)]
      (with-open [os     (Channels/newOutputStream channel)
                  writer (PrintWriter. os)
                  is     (Channels/newInputStream channel)
                  reader (InputStreamReader. is)]
        (.write writer message)
        (.read reader buffer)
        (.flip buffer)
        (str buffer))))

  (close [this conneciton]
      ;; No-op
    ))

(def unix-socket
  (->UnixSocketClient))
