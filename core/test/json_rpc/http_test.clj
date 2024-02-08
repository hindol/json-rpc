(ns json-rpc.http-test
  (:require
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [json-rpc.client :as client]
   [json-rpc.http :refer [clj-http infer-status]])
  (:import
   (clojure.lang ExceptionInfo)))

(deftest infer-status-test
  (testing "returns 200 unless error is found"
    (is (= 200 (infer-status {:result "0x0"}))))
  (testing "returns correct status code for error code"
    (doseq [[status code] [[400 -32600]
                           [404 -32601]
                           [500 -32602]
                           [500 -32603]
                           [500 -32700]
                           [500 -32099]
                           [500 -32000]]]
      (is (= status (infer-status {:error {:code code}})))))
  (testing "throw if error code is missing"
    (is (thrown? ExceptionInfo (infer-status {:error {}}))))
  (testing "throws on invalid error code"
    (is (thrown? ExceptionInfo (infer-status {:error {:code -1}}))))
  (testing "throws when error code is not a number"
    (is (thrown? ExceptionInfo (infer-status {:error {:code "-1"}})))))

(deftest ^:integration clj-http-test
  (testing "POST requests"
    (doseq [url ["http://postman-echo.com/post"
                 "https://postman-echo.com/post"]]
      (let [channel (client/open clj-http url {})]
        (try
          (let [request  {:jsonrpc "2.0"
                          :method  "eth_blockNumber"
                          :params  ["latest"]
                          :id      1}
                response (json/read-str (client/send clj-http
                                                      channel
                                                      (json/write-str request))
                                        :key-fn keyword)]
            (is (= request (:json response)))
            (is (= "application/json" (-> response :headers :content-type)))
            (is (= "application/json" (-> response :headers :accept))))
          (finally (client/close clj-http channel)))))))
