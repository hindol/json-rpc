# json-rpc

Delightful [JSON-RPC 2.0](https://www.jsonrpc.org/specification) client for Clojure(Script).

## Goals

- [x] Support *Clojure*.
- [x] Support *HTTP*.
- [x] *Future* support.
- [x] Support *WebSocket*.
- [ ] Support WebSocket *Ping/Pong*.
- [ ] Support *batching*.
- [ ] Support [*HTTP status override*](https://www.jsonrpc.org/historical/json-rpc-over-http.html#response-codes).
- [ ] Write *unit tests*.
- [ ] Pluggable *HTTP client*.
- [ ] Pluggable *JSON encoder*.
- [ ] Support *UNIX socket*.
- [ ] Support *ClojureScript*.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/com.github.hindol/json-rpc.svg)](https://clojars.org/com.github.hindol/json-rpc)

### Leiningen/Boot

```clojure
[com.github.hindol/json-rpc "${version}"]
```

### tools.deps.alpha

```clojure
com.github.hindol/json-rpc {:mvn/version "${version}"}
```

### Quickstart

```clojure
(ns example.core
  (:require [json-rpc]))

;; Choose from HTTP[S], WebSocket and UNIX socket
(def url ^:private ^:const "http://localhost:8545")

;; Coming soon!
; (def url ^:private ^:const "wss://localhost:8546")
; (def url ^:private ^:const "unix:///var/run/geth.ipc")

(def connection ^:private ^:const (json-rpc/connect url))

;; Receive a future
(json-rpc/send! connection "eth_blockNumber" ["latest"])

;; Deref to get the response. Blocks if not yet resolved.
@(json-rpc/send! connection "eth_blockNumber" ["latest"])

;; Like send! but accepts a variable number of arguments
(json-rpc/send!* connection "eth_blockNumber" "latest")
@(json-rpc/send!* connection "eth_blockNumber" "latest")
```

### Component

### Mount

### Logs

## API Documentation

[![cljdoc badge](https://cljdoc.org/badge/com.github.hindol/json-rpc)](https://cljdoc.org/d/com.github.hindol/json-rpc/CURRENT)
