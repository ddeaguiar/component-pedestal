(ns com.ddeaguiar.component-pedestal.http
  "Defines common protocols for HTTP servers.")

(defprotocol Joinable
  (join [this]
    "Joins the server thread blocking the current thread."))

(defprotocol Discoverable
  (port [this]
    "Returns the bound port of the server"))
