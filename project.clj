(defproject com.github.hindol/json-rpc "0.1.0-SNAPSHOT"
  :description "Delightful JSON-RPC 2.0 client for Clojure(Script)."
  :url "https://github.com/Hindol/json-rpc"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.logging "0.5.0"]
                 [clj-http "3.10.0"]
                 [cheshire "5.9.0"]
                 [io.pedestal/pedestal.interceptor "0.5.7"]]
  :repl-options {:init-ns json-rpc})
