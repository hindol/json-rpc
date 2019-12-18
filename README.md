# JSON-RPC 2.0

Unified [JSON-RPC 2.0](https://www.jsonrpc.org/specification) interface over HTTP\[S\], WebSocket and UNIX socket for Clojure/Script.

## Rationale

Blockchain clients including but not limited to [Go Ethereum](https://github.com/ethereum/go-ethereum), [Quorum](https://github.com/jpmorganchase/quorum) and [Besu Ethereum Client](https://github.com/hyperledger/besu/) expose their JSON-RPC 2.0 compliant API over UNIX IPC, WebSocket and HTTP\[S\]. Switching between them should be as easy as changing this,

```clojure
(def url "http://localhost:8545")
```

To this,

```clojure
(def url "ws://localhost:8546")
```

Or even this,

```clojure
(def url "unix:///var/run/geth.ipc")
```

## Goals

- [x] Support *Clojure*.
- [x] Support *HTTP*.
- [x] *Future* support.
- [x] Support *WebSocket*.
- [x] Support [*HTTP status override*](https://www.jsonrpc.org/historical/json-rpc-over-http.html#response-codes).
- [x] Support *UNIX socket*.
- [x] Write *unit tests*.
- [ ] Write *integration tests*.
- [ ] Expand *API documentation*.
- [ ] Support WebSocket *Ping/Pong*.
- [ ] Support *request batching*.
- [ ] Pluggable *HTTP client*.
- [ ] Pluggable *WebSocket client*.
- [ ] Pluggable *JSON encoder*.
- [ ] Support *ClojureScript*.
- [ ] Support *JSON-RPC notification*.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/com.github.hindol/json-rpc.core.svg)](https://clojars.org/com.github.hindol/json-rpc.core)

### [Leiningen](https://leiningen.org/)/[Boot](https://boot-clj.com/)

```clojure
[com.github.hindol/json-rpc.core "${version}"]
```

### [tools.deps.alpha](https://clojure.org/guides/deps_and_cli)

```clojure
com.github.hindol/json-rpc.core {:mvn/version "${version}"}
```

### [Minimum Viable Snippet](http://blog.fogus.me/2012/08/23/minimum-viable-snippet/)

```clojure
(ns example.core
  (:require [json-rpc.core :as rpc]))

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

## API Documentation

[![cljdoc badge](https://cljdoc.org/badge/com.github.hindol/json-rpc.core)](https://cljdoc.org/d/com.github.hindol/json-rpc.core/0.1.0-SNAPSHOT)

## Unlicense

This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>
