(ns json-rpc.ws-test
  (:require
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [json-rpc.client :as client]
   [json-rpc.ws :refer [gniazdo]]))

(deftest ^:integration gniazdo-test
  (testing "with echo response"
    (doseq [url ["ws://echo.websocket.org"
                 "wss://echo.websocket.org"]]
      (let [channel (client/open gniazdo url)]
        (try
          (let [request  {:jsonrpc "2.0"
                          :method  "eth_blockNumber"
                          :params  ["latest"]
                          :id      1}
                response (json/read-str (->> request
                                             (json/write-str)
                                             (client/send! gniazdo channel))
                                        :key-fn keyword)]
            (is (= request response)))
          (finally (client/close gniazdo channel)))))))
