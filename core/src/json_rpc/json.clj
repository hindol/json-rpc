(ns json-rpc.json
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.data.json :as json]
   [clojure.string :as str]))

(def ^:dynamic *numeric-keys*
  "The set of keys whose values are automatically converted from hexadecimal to decimal."
  #{})

(defn ^:private numeric-key?
  [k]
  (contains? *numeric-keys* k))

(defn ^:private ->hex
  [n]
  (cond->> n
    (integer? n) (format "0x%x")))

(defn ^:private ->long
  [s]
  (Long/decode s))

(defprotocol Encoder
  "A JSON encoder."
  (write-str [this m] "Takes a Clojure map as input, outputs JSON string.")
  (read-str [this s] "Takes a JSON string as input, outputs Clojure map."))

(defrecord DataJson []
  Encoder

  (write-str [this m]
    (json/write-str m
                    :value-fn (fn [k v]
                                (case k
                                  :params (mapv ->hex v)
                                  v))))

  (read-str [this s]
    (json/read-str s
                   :key-fn   csk/->kebab-case-keyword
                   :value-fn (fn [k v]
                               (cond-> v
                                 (numeric-key? k) ->long)))))

(def data-json
  "An instance of [[DataJson]]."
  (->DataJson))
