# json-rpc [![Clojars Project](https://img.shields.io/clojars/v/com.github.hindol/json-rpc.svg)](https://clojars.org/com.github.hindol/json-rpc) [![cljdoc badge](https://cljdoc.org/badge/com.github.hindol/json-rpc)](https://cljdoc.org/d/com.github.hindol/json-rpc/CURRENT)

Delightful [JSON-RPC 2.0](https://www.jsonrpc.org/specification) client for Clojure(Script).

## Goals

- [x] Support *Clojure*.
- [x] Support *HTTP*.
- [x] *Future* support.
- [ ] Support [*HTTP status override*](https://www.jsonrpc.org/historical/json-rpc-over-http.html#response-codes).
- [ ] Write *unit tests*.
- [ ] Pluggable *HTTP client*.
- [ ] Support *WebSocket*.
- [ ] Support *UNIX socket*.
- [ ] Support *ClojureScript*.

## Usage

### Leiningen

### Boot

### tools.deps.alpha

### Quickstart

```clojure
;; Choose from HTTP[S], WebSocket and UNIX socket
(def url ^:private ^:const "http://localhost:8545")
(def url ^:private ^:const "wss://localhost:8546")
(def url ^:private ^:const "unix:///var/run/geth.ipc")

(def connection ^:private ^:const (json-rpc/connect url))

(json-rpc/send! connection "eth_blockNumber" ["latest"])
@(json-rpc/send! connection "eth_blockNumber" ["latest"])

;; Like send! but accepts a variable number of arguments
(json-rpc/send!* connection "eth_blockNumber" "latest")
@(json-rpc/send!* connection "eth_blockNumber" "latest")
```

### Component

### Mount

### Logs
