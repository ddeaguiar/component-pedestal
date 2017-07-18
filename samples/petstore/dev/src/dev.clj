(ns dev
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer :all]
   [clojure.spec.alpha :as s]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]
   [clojure.test :refer [run-all-tests]]
   [com.stuartsierra.component.repl :refer [reset set-init start stop system]]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [cheshire.core :as json]
   [petstore.system :as system]
   [com.ddeaguiar.component-pedestal :as cp]))

(defn dev-system
  [_]
  (println "\nCreating your [DEV] server...")
  (-> (system/system)
      (update-in [:pedestal :service]
                 merge
                 {:env                   :dev
                  ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})
      (update :pedestal cp/with-dev-mode)))

(set-init dev-system)
