(ns petstore.server
  (:gen-class) ; for -main method in uberjar
  (:require [io.pedestal.http :as http]
            [petstore.system :as system]
            [com.stuartsierra.component :as component]
            [com.ddeaguiar.component-pedestal :as cp]))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (component/start (system/system)))
