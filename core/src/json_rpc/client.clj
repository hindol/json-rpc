(ns json-rpc.client
  (:refer-clojure :exclude [send]))

(defprotocol Client
  "A JSON-RPC client."
  (open [this url headers] "Opens a connection to the given URL")
  (send [this conneciton message] "Sends a JSON-RPC request to the open connection")
  (close [this conneciton] "Closes the connection"))
