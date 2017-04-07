(ns com.ddeaguiar.component-pedestal-test
  (:require
   [clojure.test :refer :all]
   [com.ddeaguiar.component-pedestal :as cp]
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.test :refer [response-for]]
   [io.pedestal.http.body-params :as body-params]
   [ring.util.response :as ring-resp]
   [io.pedestal.interceptor :as interceptor]
   [io.pedestal.http.route :as route]))

(defmacro with-system
  "Binds the var to the result of the binding expression,
  which should be a valid component system. Starts the
  system prior to evaluating the body expression. Stops
  the system after the body has been evaluated.

  Example usage:

  (deftest some-test
      (with-system [test-system (my-test-system-init-fn)]
        (let [service (pedestal/service-fn (:pedestal test-system))
              response (response-for service :get \"/\")]
          (is (= 200 (:status response))))))"
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(defn root
  [req]
  (ring-resp/response "Root handler"))

(defn user
  [req]
  (let [db        (get-in req [::cp/deps :db])
        id        (get-in req [:path-params :id])
        user-name (get db id "not found")]
    (ring-resp/response (str "User name is: " user-name))))

(def user-interceptor
  {:name  ::user-interceptor
   :enter (fn [ctx]
            (let [db   (get-in ctx [::cp/deps :db])
                  id   (get-in ctx [:request :path-params :id])
                  user (get db id "not found")]
              (assoc-in ctx [:request :user] user)))})

(defn user2
  [req]
  (let [user (:user req)]
    (ring-resp/response (str "User name is: " user))))

(def common-interceptors [(body-params/body-params) http/html-body])

(def routes
  #{["/" :get (conj common-interceptors `root)]
    ["/user/:id" :get (conj common-interceptors (cp/ref :user-handler))]
    ["/user2/:id" :get (into common-interceptors [(cp/ref :user-interceptor) `user2])]})

(def system (component/system-map
             :db {"dan"  "Daniel De Aguiar"
                  "yogi" "Yogi Bear"}
             :user-handler (component/using
                            (cp/component-handler `user)
                            [:db])
             :user-interceptor (component/using
                                (cp/component-interceptor user-interceptor)
                                [:db])
             :service {:env                     :test
                       ::http/routes            routes
                       ::http/resource-path     "/public"
                       ::http/type              :jetty
                       ::http/port              0
                       ::http/container-options {:h2c? true
                                                 :h2?  false
                                                 :ssl? false}}
             :pedestal (component/using
                        (cp/component-pedestal)
                        [:service
                         :user-handler
                         :user-interceptor])))

(def url-for (-> routes
                 (cp/resolve-refs system)
                 route/expand-routes
                 route/url-for-routes))

(deftest component-pedestal-test
  (with-system [test-system system]
    (let [service (cp/service-fn (:pedestal test-system))]
      (is (= "Root handler"
             (:body (response-for service :get (url-for ::root)))))
      (is (= "User name is: Daniel De Aguiar"
             (:body (response-for service
                                  :get
                                  (url-for ::user
                                           :path-params
                                           {:id "dan"})))))
      (is (= "User name is: not found"
             (:body (response-for service
                                  :get
                                  (url-for ::user
                                           :path-params
                                           {:id "foo"})))))
      (is (= "User name is: Yogi Bear"
             (:body (response-for service
                                  :get
                                  (url-for ::user2
                                           :path-params
                                           {:id "yogi"})))))
      (is (= "User name is: not found"
             (:body (response-for service
                                  :get
                                  (url-for ::user2
                                           :path-params
                                           {:id "foo"})))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Ref ':user-handlr' was not resolved. Did you mean ':user-handler'?"
                            (cp/resolve-refs #{["/user" :get (cp/ref :user-handlr)]}
                                             test-system))))))
