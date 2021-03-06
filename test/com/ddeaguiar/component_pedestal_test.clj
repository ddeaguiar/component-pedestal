(ns com.ddeaguiar.component-pedestal-test
  (:require
   [clojure.test :refer :all]
   [com.ddeaguiar.component-pedestal :as component-pedestal]
   [com.ddeaguiar.component-pedestal.test-helper :refer [*url-for* *service* with-service]]
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.test :refer [response-for]]
   [io.pedestal.http.body-params :as body-params]
   [ring.util.response :as ring-resp]))

(defprotocol UserStore
  (find-user [this id]))

(defn user-handler
  [req]
  (let [user-store (::component-pedestal/component req)
        id         (get-in req [:path-params :id])
        user-name  (or (find-user user-store id) "not found")]
    (ring-resp/response (str "User name is: " user-name))))


(defrecord Users [db]
  component-pedestal/RouteProvider
  (routes [this]
    #{["/user/:id" :get (conj [(body-params/body-params)
                               http/html-body
                               (component-pedestal/attach this)]
                              `user-handler) :route-name ::user]})

  UserStore
  (find-user [_ id]
    (get db id)))

(defn test-system
  []
  (component/system-map
   :db {"dan"  "Daniel De Aguiar"
        "yogi" "Yogi Bear"}
   :user-store (component/using
                (map->Users {})
                [:db])
   :pedestal (component/using
              (component-pedestal/with-automatic-port (component-pedestal/component-pedestal))
              [:user-store])))

(deftest component-pedestal-test
  (with-service (test-system) :pedestal
    (is (= "User name is: Daniel De Aguiar"
           (:body (response-for *service*
                                :get
                                (*url-for* ::user
                                           :path-params
                                           {:id "dan"})))))
    (is (= "User name is: not found"
           (:body (response-for *service*
                                :get
                                (*url-for* ::user
                                           :path-params
                                           {:id "foo"})))))))
