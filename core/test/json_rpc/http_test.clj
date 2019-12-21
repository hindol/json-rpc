(ns json-rpc.http-test
  (:require
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [json-rpc.client :as client]
   [json-rpc.http :refer [clj-http]]))

(deftest ^:integration clj-http-test
  (testing "POST requests"
    (let [channel  (client/open clj-http "https://postman-echo.com/post")]
      (try
        (let [request  {:jsonrpc "2.0"
                        :method  "eth_blockNumber"
                        :params  ["latest"]
                        :id      1}
              response (json/read-str (client/send! clj-http
                                                    channel
                                                    (json/write-str request))
                                      :key-fn keyword)]
          (is (= request (:json response)))
          (is (= "application/json" (-> response :headers :content-type)))
          (is (= "application/json" (-> response :headers :accept))))
        (finally (client/close clj-http channel))))))
