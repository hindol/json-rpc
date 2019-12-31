# JSON-RPC 2.0 ![](https://github.com/Hindol/json-rpc/workflows/Clojure%20CI/badge.svg)

Unified [JSON-RPC 2.0](https://www.jsonrpc.org/specification) interface over HTTP\[S\], WebSocket and UNIX socket for Clojure/Script.

## Rationale

Blockchain clients including but not limited to [Go Ethereum](https://github.com/ethereum/go-ethereum), [Quorum](https://github.com/jpmorganchase/quorum) and [Besu Ethereum Client](https://github.com/hyperledger/besu/) expose their JSON-RPC 2.0 compliant API over UNIX IPC, WebSocket and HTTP\[S\]. Switching between them should be as easy as changing `(def url "http://localhost:8545")` to `(def url "ws://localhost:8546")` or `(def url "unix:///var/run/geth.ipc")`.

## Goals

- [x] Unified interface over HTTP\[S\], WS\[S\] and UNIX socket
- [x] Unit tests
- [ ] Integration tests
- [ ] WebSocket keep alive (ping/pong)
- [ ] Examples
- [ ] Request batching
- [ ] Add support for ClojureScript

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
(def url "http://localhost:8545")    ;; Or https://
(def url "ws://localhost:8546")      ;; Or wss://
(def url "unix:///var/run/geth.ipc")

(def channel (rpc/open url))

(rpc/send channel "eth_blockNumber" [])
;; => {:result "0x14eca", :id "6fd9a7a8-c774-4b76-a61e-6802ae64e212"}

;; Finally
(rpc/close channel)
```

Or, if you prefer,

```clojure
(with-open [channel (rpc/open "http://localhost:8545")]
  (rpc/send channel "eth_blockNumber" []))
  ;; => {:result "0x14eca", :id "6fd9a7a8-c774-4b76-a61e-6802ae64e212"}
```

If ID is not supplied, an auto-generated UUID will be used. Set ID explicitly using the optional `:id` key on `send`,

```clojure
(with-open [channel (rpc/open "http://localhost:8545")]
  (rpc/send channel "eth_blockNumber" [] :id 1))
  ;; => {:result "0x14eca", :id 1}
```

## API Documentation

[![cljdoc badge](https://cljdoc.org/badge/com.github.hindol/json-rpc.core)](https://cljdoc.org/d/com.github.hindol/json-rpc.core/CURRENT)

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
