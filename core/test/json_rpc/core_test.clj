(ns json-rpc.core-test
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [json-rpc.core :refer [connect decode encode uuid version]])
  (:import
   (clojure.lang ExceptionInfo)))

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

(deftest connect-test

  (testing "HTTP, HTTPS, WS and WSS"
    (doseq [input ["http://www.microsoft.com"
                   "https://www.microsoft.com"]]
      (let [{:keys [scheme url]} (connect input)]
        (is (= :http scheme))
        (is (= input url))))
    (doseq [input ["ws://www.microsoft.com"
                   "wss://www.microsoft.com"]]
      (let [{:keys [scheme url]} (connect input)]
        (is (= :ws scheme))
        (is (= input url)))))

  (testing "UNIX socket paths"
    (let [{:keys [scheme path]} (connect "unix:///var/run/geth.ipc")]
      (is (= :unix scheme))
      (is (= "/var/run/geth.ipc" path))))

  (testing "exception on unsupported scheme"
    (is (thrown? ExceptionInfo (connect "file:///var/run/geth.ipc")))))
