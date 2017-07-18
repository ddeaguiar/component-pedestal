(ns petstore.users
  (:require
   [com.stuartsierra.component :as component]
   [com.ddeaguiar.component-pedestal :as component.pedestal]
   [datomic.api :as d]
   [io.pedestal.http :as http]
   [ring.util.response :as ring-resp]
   [petstore.auth :as auth]
   [petstore.protocols :as protocols]))

(defrecord UserStore [db]
  protocols/AuthStore
  (user-identity [_ username]
    ;; TODO: queries db for user by username
    ;; where db is a datomic component.
    {:auth/username "j@s.com"
     :auth/password "pbkdf2+sha512$d675a39bd6faa484d8f22209$10000$13f1f69c4ebff10e69de3dc2d52c4d74da782e73eeb62820cc5fc95709569328bd67d64b4f01156b4b90de6730d6ba0e9c471578f427677e2cadf52f08d74696"}))

(defn make-user-store []
  (map->UserStore {}))
