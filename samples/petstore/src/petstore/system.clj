(ns petstore.system
  (:require [com.stuartsierra.component :as component]
            [petstore.auth :as auth]
            [petstore.users :as users]
            [com.ddeaguiar.component-pedestal :as component.pedestal]
            [petstore.service :as service]))

(defn system
  []
  (component/system-map
   :db {}
   :user-store (component/using (users/make-user-store)
                                [:db])
   :auth (component/using (auth/make-auth-component)
                          {:auth-store :user-store})
   :pedestal (component/using (component.pedestal/component-pedestal service/service)
                              [:auth :user-store])))
