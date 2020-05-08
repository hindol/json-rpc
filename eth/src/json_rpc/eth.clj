(ns json-rpc.eth
  (:require
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [clojure.walk :as w]
   [json-rpc.core :as rpc]))

(defn ^:private ->hex
  [n]
  (cond->> n
    (integer? n) (format "0x%x")))

(defn ^:private ->long
  [s]
  (cond->> s
    (and (string? s)
         (str/starts-with? s "0x")) Long/decode))

(defn numerify-values
  "Given a map 'm' and a set of keys 'ks', convert the value to a number
  for all keys in 'ks'."
  [m ks]
  (w/walk (fn [[k v]]
            (if (contains? ks k)
              [k (->long v)]
              [k v]))
          identity
          m))

(defn send
  ([channel method params] (send channel method params {}))
  ([channel method params {:keys [pre-fn post-fn]
                           :or   {pre-fn  identity
                                  post-fn identity}}]
   (let [params   (mapv pre-fn params)
         response (rpc/send channel method params)]
     (if-some [result (:result response)]
       (post-fn result)
       (:error response)))))

(defn protocol-version
  [channel]
  (send channel "eth_protocolVersion" []))

(defn syncing
  [channel]
  (send channel "eth_syncing" []
        {:post-fn #(numerify-values % #{:starting-block
                                        :current-block
                                        :highest-block})}))

(defn coinbase
  [channel]
  (send channel "eth_coinbase" []))

(defn mining?
  [channel]
  (send channel "eth_mining" []))

(defn hashrate
  [channel]
  (send channel "eth_hashrate" [] {:post-fn ->long}))

(defn gas-price
  [channel]
  (send channel "eth_gasPrice" [] {:post-fn ->long}))

(defn accounts
  [channel]
  (send channel "eth_accounts" []))

(defn block-number
  [channel]
  (send channel "eth_blockNumber" [] {:post-fn ->long}))

(defn get-balance
  [channel account tag-or-number]
  (send channel "eth_getBalance" [account tag-or-number] {:pre-fn ->hex
                                                          :post-fn ->long}))

(defn get-storage-at
  [channel address position tag-or-number]
  (send channel "eth_getStorageAt" [address position tag-or-number]
        {:pre-fn ->hex}))

(defn get-transaction-count
  [channel account tag-or-number]
  (send channel "eth_getTransactionCount" [account tag-or-number]
        {:pre-fn  ->hex
         :post-fn ->long}))

(defn get-block
  ([channel number-or-hash] (get-block channel number-or-hash {}))
  ([channel number-or-hash {:keys [transactions?]
                            :or   {transactions? false}}]
   (let [method (if (integer? number-or-hash)
                  "eth_getBlockByNumber"
                  "eth_getBlockByHash")]
     (send channel method [number-or-hash transactions?]
           {:pre-fn  ->hex
            :post-fn #(numerify-values % #{:difficulty
                                           :gas-limit
                                           :gas-used
                                           :number
                                           :size
                                           :timestamp
                                           :total-difficulty})}))))
