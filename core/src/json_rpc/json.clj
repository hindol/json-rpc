(ns json-rpc.json
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.data.json :as json]
   [clojure.string :as str]))

(defprotocol Encoder
  "A JSON encoder."
  (write-str [this m] "Takes a Clojure map as input, outputs JSON string.")
  (read-str [this s] "Takes a JSON string as input, outputs Clojure map."))

(defrecord DataJson []
  Encoder

  (write-str [this m]
    (json/write-str m))

  (read-str [this s]
    (json/read-str s :key-fn csk/->kebab-case-keyword)))

(def data-json
  "An instance of [[DataJson]]."
  (->DataJson))
