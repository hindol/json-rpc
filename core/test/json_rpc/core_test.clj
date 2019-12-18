(ns json-rpc.core-test
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [json-rpc.core :refer [connect decode encode send! uuid version]]
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

(deftest connect-test

  (testing "HTTP, HTTPS, WS and WSS"
    (let [input                "http://www.microsoft.com"
          {:keys [scheme url]} (connect input)]
      (is (= :http scheme))
      (is (= input url)))
    (let [input                "https://www.microsoft.com"
          {:keys [scheme url]} (connect input)]
      (is (= :http scheme))
      (is (= input url)))
    (let [input                "ws://www.microsoft.com"
          {:keys [scheme url]} (connect input)]
      (is (= :ws scheme))
      (is (= input url)))
    (let [input                "wss://www.microsoft.com"
          {:keys [scheme url]} (connect input)]
      (is (= :ws scheme))
      (is (= input url))))

  (testing "UNIX socket paths"
    (let [{:keys [scheme path]} (connect "unix:///var/run/geth.ipc")]
      (is (= :unix scheme))
      (is (= "/var/run/geth.ipc" path))))

  (testing "exception on unsupported scheme"
    (is (thrown? ExceptionInfo (connect "file:///var/run/geth.ipc")))))

(deftest send!-test
  (let [response (json/write-str {:jsonrpc "2.0"
                                  :result  "0x0"
                                  :id      1})]
    (testing "HTTP requests"
      (let [client (mock http/Client {:post! response})]
        (is (not (received? client http/post!)))
        (is (= {:result "0x0"
                :id     1}
               @(send! {:scheme :http
                        :url    "http://www.microsoft.com"}
                       client
                       "eth_blockNumber"
                       ["latest"])))
        (is (received? client http/post!))))
    (testing "WS requests"
      (let [client (mock ws/Client {:write! response})]
        (is (not (received? client ws/write!)))
        (is (= {:result "0x0"
                :id     1}
               @(send! {:scheme :ws
                        :url    "ws://www.microsoft.com"}
                       client
                       "eth_blockNumber"
                       ["latest"])))
        (is (received? client ws/write!))))
    (testing "UNIX sockets"
      (let [client (mock unix/Client {:write! response})]
        (is (not (received? client unix/write!)))
        (is (= {:result "0x0"
                :id     1}
               @(send! {:scheme :unix
                        :path   "/var/run/geth.ipc"}
                       client
                       "eth_blockNumber"
                       ["latest"])))
        (is (received? client unix/write!))))))
