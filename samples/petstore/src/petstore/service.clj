(ns petstore.service
  (:require [io.pedestal.http :as http]))

(def service {:env                     :prod
              ::http/resource-path     "/public"
              ::http/type              :jetty
              ::http/port              8080
              ::http/enable-session    {}
              ::http/join? false
              ::http/container-options {:h2c? true
                                        :h2?  false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})
