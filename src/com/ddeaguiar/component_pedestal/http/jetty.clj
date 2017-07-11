(ns com.ddeaguiar.component-pedestal.http.jetty
  (:require [com.ddeaguiar.component-pedestal.http :as cp.http])
  (:import (org.eclipse.jetty.server Server)))

(extend-type Server
  cp.http/Joinable
  (cp.http/join [s]
    (.join ^Server s))

  cp.http/Discoverable
  (cp.http/port [s]
    (some-> ^Server s
            .getConnectors
            (aget 0)
            .getLocalPort)))
