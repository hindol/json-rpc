(ns json-rpc.url-test
  (:require
   [clojure.test :refer [are deftest is testing]]
   [json-rpc.url :refer [path scheme]]))

(deftest scheme-test

  (testing "with HTTP and HTTPS URL"
    (are [expected parsed] (= expected parsed)
      :http (scheme "http://www.microsoft.com")
      :https (scheme "https://www.microsoft.com")))

  (testing "with WS and WSS URL"
    (are [expected parsed] (= expected parsed)
      :ws (scheme "ws://www.microsoft.com")
      :wss (scheme "wss://www.microsoft.com"))))

(deftest path-test
  (testing "with socket URL"
    (is (= "/var/run/geth.ipc" (path "unix:///var/run/geth.ipc"))))
  (testing "with HTTP URL"
    (is (= "www.microsoft.com" (path "http://www.microsoft.com")))))