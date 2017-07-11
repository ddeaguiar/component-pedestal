(ns com.ddeaguiar.component-pedestal.http.tomcat
  (:require [com.ddeaguiar.component-pedestal.http :as cp.http])
  (:import (org.apache.catalina.startup Tomcat)))

(extend-type Tomcat
  cp.http/Joinable
  (cp.http/join [s]
    (.await (.getServer ^Tomcat s)))

  cp.http/Discoverable
  (cp.http/port [s]
    (.getLocalPort (.getConnector ^Tomcat s))))
