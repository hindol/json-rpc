(ns json-rpc.http-test
  (:require
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [json-rpc.http :refer [clj-http post!]]))

(deftest ^:integration clj-http-test
  (testing "POST requests"
    (let [request  {:jsonrpc "2.0"
                    :method  "eth_blockNumber"
                    :params  ["latest"]
                    :id      1}
          response (post! clj-http
                          "https://postman-echo.com/post"
                          (json/write-str request))
          body     (-> response
                       :body
                       (json/read-str :key-fn keyword))]
      (is (= request (:json body)))
      (is (= "application/json" (-> body :headers :content-type)))
      (is (= "application/json" (-> body :headers :accept))))))
