(ns petstore.auth
  "Authentication/Authorization implementation."
  (:require
   [buddy.auth]
   [buddy.auth.backends :as auth.backends]
   [buddy.auth.middleware :as auth.middleware]
   [buddy.hashers]
   [io.pedestal.http :as http]
   [io.pedestal.interceptor :as interceptor]
   [io.pedestal.log :as log]
   [com.ddeaguiar.component-pedestal :as component.pedestal]
   [ring.util.response :as ring-resp]
   [io.pedestal.http.body-params :as body-params]
   [com.stuartsierra.component :as component]
   [petstore.protocols :as protocols]))

;; -- Interceptors --

(defn authentication-interceptor
  "Port of buddy-auth's wrap-authentication middleware"
  [auth-provider]
  (interceptor/interceptor
   {:name  ::authenticate
    :enter (fn [ctx]
             (let [new-ctx (update ctx :request auth.middleware/authentication-request (:backend auth-provider))]
               new-ctx))}))

(def attach-credentials
  "Attaches login credentials to the current session."
  (interceptor/interceptor
   {:name ::attach-credentials
    :enter (fn
             [ctx]
             (let [creds (get-in ctx [:request :json-params])]
               (if (and creds (every? #{:username :password} (keys creds)))
                 (update-in ctx [:request :session] merge {:identity creds})
                 ctx)))}))

(def common-interceptors [(body-params/body-params)
                          http/json-body])

;; -- Handlers --

(defn login
  "Login handler"
  [req]
  (-> req
      :identity
      (dissoc :auth/password)
      ring-resp/response))

(defn logout [req]
  (log/info :req req)
  (-> (ring-resp/response {})
      (assoc :session {})))

;; -- Components --

(defrecord HashProvider [opts]
  protocols/Hasher
  (derive [_ s] (buddy.hashers/derive s opts))
  (check [_ attempt encrypted] (buddy.hashers/check attempt encrypted)))

(defn make-hash-provider
  ([]
   (make-hash-provider {:alg        :pbkdf2+sha512
                        :iterations 10000}))
  ([opts]
   (map->HashProvider {:opts opts})))

(defrecord AuthProvider [auth-store hash-provider backend]
  component/Lifecycle
  (start [this]
    (if backend
      this
      (do
        (log/info :msg "Starting the AuthProvider.")
        (assoc this :backend (auth.backends/session {:authfn (partial protocols/authenticate this)})))))

  (stop [this]
    (log/info :msg "Stopping the AuthProvider")
    (assoc this :backend nil))

  protocols/Hasher
  (derive [_ s] (protocols/derive hash-provider s))

  (check [_ attempt encrypted] (protocols/check hash-provider attempt encrypted))

  protocols/Authenticate
  (authenticate [this creds]
    (let [{:keys [username password]} creds
          user (protocols/user-identity auth-store username)]
      (when (and user (protocols/check this password (:auth/password user)))
        user)))

  component.pedestal/RouteProvider
  (routes [this]
    #{["/login" :post (into common-interceptors [(component.pedestal/attach this :name ::attach-auth)
                                                 attach-credentials
                                                 (authentication-interceptor this)
                                                 `login])]
      ["/logout" :get (conj common-interceptors `logout)]}))

(defn make-auth-component
  "AuthProvider ctor."
  ([]
   (make-auth-component {:hash-provider (make-hash-provider)}))
  ([m]
   (map->AuthProvider m)))
