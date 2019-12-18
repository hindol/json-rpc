(ns json-rpc.ws-test
  (:require
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [json-rpc.core :as core]
   [json-rpc.ws :refer [gniazdo write!]]))

(deftest ^:integration gniazdo-test
  (testing "with echo response"
    (let [connection (core/connect "ws://echo.websocket.org")
          request    {:jsonrpc "2.0"
                      :method  "eth_blockNumber"
                      :params  ["latest"]
                      :id      1}
          response   (json/read-str (->> request
                                         (json/write-str)
                                         (write! gniazdo connection))
                                    :key-fn keyword)]
      (is (= request response)))))
