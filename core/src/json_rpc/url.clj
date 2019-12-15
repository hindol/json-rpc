(ns json-rpc.url
  (:require
   [clojure.string :as string]))

(defn scheme
  "Extracts the scheme from an URL."
  [url]
  (let [[scheme _] (string/split url #"://")]
    scheme))

(defn path
  "Extracts the path from an URL."
  [url]
  (let [[_ path] (string/split url #"://")]
    path))