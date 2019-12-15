(ns json-rpc.core-test
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [json-rpc.core :refer [decode encode uuid version]]))

(def uuid-regex
  #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

(deftest uuid-test
  (testing "UUID is valid"
    (let [uuid (str/lower-case (uuid))]
      (is (re-matches uuid-regex uuid)))))

(deftest encode-test
  (testing "with ID"
    (let [method "eth_blockNumber"
          params ["latest"]
          id     1]
      (is (= (json/read-str (encode method params id)
                            :key-fn keyword)
             {:jsonrpc version
              :method  method
              :params  params
              :id      id}))))
  (testing "without ID"
    (let [method "eth_blockNumber"
          params ["latest"]
          id     1]
      (is (= (json/read-str (encode method params id)
                            :key-fn keyword)
             {:jsonrpc version
              :method  method
              :params  params
              :id      id})))))

(deftest decode-test
  (testing "response with result"
    (is (= (decode (json/write-str {:jsonrpc version
                                    :result  "0x0"
                                    :id      1}))
           {:result "0x0"})))
  (testing "response with error"
    (is (= (decode (json/write-str {:jsonrpc version
                                    :error   {:code    -32602
                                              :message "Method not found"}}))
           {:error {:code    -32602
                    :message "Method not found"}}))))
