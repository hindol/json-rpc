(ns json-rpc.core-test
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [json-rpc.core :refer [close decode encode send! open route uuid version]]
   [json-rpc.http :as http]
   [json-rpc.unix :as unix]
   [json-rpc.ws :as ws]
   [shrubbery.core :refer [mock received?]])
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
          params ["latest"]]
      (is (= (dissoc (json/read-str (encode method params)
                                    :key-fn keyword)
                     :id)
             {:jsonrpc version
              :method  method
              :params  params})))))

(deftest decode-test
  (testing "response with result"
    (is (= (decode (json/write-str {:jsonrpc version
                                    :result  "0x0"
                                    :id      1}))
           {:result "0x0"
            :id     1})))
  (testing "response with error"
    (is (= (decode (json/write-str {:jsonrpc version
                                    :error   {:code    -32602
                                              :message "Method not found"}
                                    :id      1}))
           {:error {:code    -32602
                    :message "Method not found"}
            :id    1}))))

(deftest route-test
  (testing "router returns correct record for scheme"
    (is (= http/clj-http (route "http://postman-echo.com/post")))
    (is (= http/clj-http (route "https://postman-echo.com/post")))
    (is (= ws/gniazdo (route "ws://echo.websocket.org")))
    (is (= ws/gniazdo (route "wss://echo.websocket.org")))
    (is (= unix/unix-socket (route "unix:///var/run/geth.ipc")))))

(deftest ^:integration open-test
  (testing "open can open channels for all supported schemes"
    (doseq [url ["http://postman-echo.com/post"
                 "https://postman-echo.com/post"
                 "ws://echo.websocket.org"
                 "wss://echo.websocket.org"
                 "unix:///var/run/geth.ipc"]]
      (let [channel (open url)]
        (is (= [:send!-fn :close-fn] (keys channel)))
        (close channel))))
  (testing "open throws exception on unsupported schemes"
    (is (thrown? ExceptionInfo (open "file:///var/run/geth.ipc")))))
