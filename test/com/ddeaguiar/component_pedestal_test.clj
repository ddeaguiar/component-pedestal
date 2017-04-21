(ns com.ddeaguiar.component-pedestal-test
  (:require
   [clojure.test :refer :all]
   [com.ddeaguiar.component-pedestal :as component-pedestal]
   [com.ddeaguiar.component-pedestal.test-helper :refer [*url-for* *service* with-service]]
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.test :refer [response-for]]
   [io.pedestal.http.body-params :as body-params]
   [ring.util.response :as ring-resp]
   [io.pedestal.interceptor :as interceptor]
   [io.pedestal.http.route :as route]))

(defprotocol UserStore
  (find-user [this id]))

(defn user-handler
  [user-store]
  (fn [req]
    (let [id        (get-in req [:path-params :id])
          user-name (or (find-user user-store id) "not found")]
      (ring-resp/response (str "User name is: " user-name)))))

(def common-interceptors [(body-params/body-params) http/html-body])


(defrecord Users [db]
  component-pedestal/RouteProvider
  (routes [this]
    #{["/user/:id" :get (conj common-interceptors (user-handler this)) :route-name ::user]})

  UserStore
  (find-user [_ id]
    (get db id)))

(def system (component/system-map
             :db {"dan"  "Daniel De Aguiar"
                  "yogi" "Yogi Bear"}
             :user-store (component/using
                          (map->Users {})
                          [:db])
             :pedestal (component/using
                        (component-pedestal/with-automatic-port (component-pedestal/component-pedestal))
                        [:user-store])))

(deftest component-pedestal-test
  (with-service system :pedestal
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
