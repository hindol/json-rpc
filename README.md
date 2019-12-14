# JSON-RPC 2.0

Unified [JSON-RPC 2.0](https://www.jsonrpc.org/specification) interface over HTTP\[S\], WebSocket and UNIX socket for Clojure(Script).

## Rationale

## Goals

- [x] Support *Clojure*.
- [x] Support *HTTP*.
- [x] *Future* support.
- [x] Support *WebSocket*.
- [x] Support [*HTTP status override*](https://www.jsonrpc.org/historical/json-rpc-over-http.html#response-codes).
- [ ] Write *unit tests*.
- [ ] Support *UNIX socket*.
- [ ] Support WebSocket *Ping/Pong*.
- [ ] Support *request batching*.
- [ ] Pluggable *HTTP client*.
- [ ] Pluggable *WebSocket client*.
- [ ] Pluggable *JSON encoder*.
- [ ] Support *ClojureScript*.
- [ ] Support *JSON-RPC notification*.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/com.github.hindol/json-rpc.svg)](https://clojars.org/com.github.hindol/json-rpc)

### [Leiningen](https://leiningen.org/)/[Boot](https://boot-clj.com/)

```clojure
[com.github.hindol/json-rpc "${version}"]
```

### [tools.deps.alpha](https://clojure.org/guides/deps_and_cli)

```clojure
com.github.hindol/json-rpc {:mvn/version "${version}"}
```

### Quickstart

```clojure
(ns example.core
  (:require [json-rpc :as rpc]))

;; Choose from HTTP[S], WebSocket and UNIX socket
(def url "http://localhost:8545")
(def url "ws://localhost:8546")
(def url "unix:///var/run/geth.ipc")

(def connection (rpc/connect url))

;; Receive a future
(rpc/send! connection "eth_blockNumber" ["latest"])

;; Deref to get the response
@(rpc/send! connection "eth_blockNumber" ["latest"])

;; Like send! but accepts a variable number of arguments
@(rpc/send!* connection "eth_blockNumber" "latest")
```

### Component

### Mount

### Logs

## API Documentation

[![cljdoc badge](https://cljdoc.org/badge/com.github.hindol/json-rpc)](https://cljdoc.org/d/com.github.hindol/json-rpc/CURRENT)
