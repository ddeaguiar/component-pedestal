(ns com.ddeaguiar.component-pedestal.http.immutant
  (:require [com.ddeaguiar.component-pedestal.http :as cp.http])
  (:import (org.projectodd.wunderboss.web.undertow UndertowWeb)))

(extend-type UndertowWeb
  cp.http/Joinable
  ;; Unclear what to do here with Undertow
  (cp.http/join [s])

  cp.http/Discoverable
  ;; The version of Undertow depended on
  ;; does not support an easy way to do this.
  ;; Refer to https://issues.jboss.org/browse/UNDERTOW-628?_sscc=t
  ;; This is resolved in the 1.4 release.
  (cp.http/port [s]))
