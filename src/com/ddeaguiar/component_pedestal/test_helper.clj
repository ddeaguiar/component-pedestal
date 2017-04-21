(ns com.ddeaguiar.component-pedestal.test-helper
  (:require [com.stuartsierra.component :as component]
            [com.ddeaguiar.component-pedestal :as component-pedestal]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(def ^:dynamic *service*
  "The service-fn for the started pedestal component."
  nil)
(def ^:dynamic *url-for*
  "url-for helper for the started pedestal component."
  nil)

(defmacro with-service
  "Executes body within a binding of *current-service* to the
  service-fn of the pedestal component identified by pedestal-k in the
  started system based on a-system."
  [a-system pedestal-k & body]
  `(let [started-system# (component/start ~a-system)]
     (binding [*service* (-> started-system#
                             (get ~pedestal-k)
                             component-pedestal/service-fn)
               *url-for* (-> started-system#
                             (get-in [~pedestal-k :service ::http/routes])
                             route/expand-routes
                             route/url-for-routes)]
       (try
         ~@body
         (finally
           (component/stop started-system#))))))
