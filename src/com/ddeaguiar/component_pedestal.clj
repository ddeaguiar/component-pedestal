(ns com.ddeaguiar.component-pedestal
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.log :as log]
            [io.pedestal.http.route :as route]))

(defprotocol RouteProvider
  (routes [this]
    "A collection of un-expanded routes."))

(defprotocol Service
  (service-fn [this]
    "The service fn."))

(defrecord Pedestal [service]
  component/Lifecycle
  (start [this]
    (if (service-fn this)
      this
      (let [route-providers (filter #(satisfies? RouteProvider %) (vals this))
            routes          (->> route-providers
                                 (map routes)
                                 (reduce into))]
        (log/info :msg "Starting Pedestal." :port (::http/port service))
        (assoc this
               :service
               (-> service
                   (assoc ::http/routes routes)
                   http/default-interceptors
                   (cond-> (::dev? this) http/dev-interceptors)
                   http/create-server
                   http/start)))))
  (stop [this]
    (log/info :msg "Stopping Pedestal.")
    (try
      (http/stop service)
      (catch Throwable t
        (log/error :msg "Error stopping Pedestal." :exception t)))
    (assoc this :service nil))

  Service
  (service-fn [_]
    (::http/service-fn service)))

(defn service-map
  "Returns initial service map for the Pedestal server."
  []
  {::http/resource-path     "/public"
   ::http/type              :jetty
   ::http/port              8080
   ::http/join?             false
   ::http/container-options {:h2c? true
                             :h2?  false
                             :ssl? false}})

(defn component-pedestal
  "Pedestal component ctor."
  [] (map->Pedestal {:service (service-map)}))

(defn with-port
  "Updates the pedestal component to bind to port. Must be called
  before start."
  [component port]
  (assoc-in component [:service ::http/port] port))

(defn with-automatic-port
  "Updates pedestal component to bind to a random free port. Must be called
  before start."
  [component]
  (with-port component 0))

(defn with-container-options
  "Updates the pedestal component with container-options. Must be called
  before start."
  [component container-options]
  (assoc-in component [:service ::http/container-options] container-options))

(defn with-resource-path
  "Updates the pedestal component with resource-path. Must be called
  before start."
  [component resource-path]
  (assoc-in component [:service ::http/resource-path] resource-path))

(defn with-dev-mode
  "Adds development-mode interceptors to the pedestal component. Must
  be called before start."
  [component]
  (assoc component ::dev? true))

(defn port
  "Returns bound port of the (started) pedestal component."
  [component]
  (some-> component :service ::http/server
          .getConnectors (aget 0) .getLocalPort))

(defn join
  "Joins the server thread, blocking the current thread."
  [component]
  (.join ^org.eclipse.jetty.server.Server
         (get-in component [:service ::http/server])))

(defn attach
  "Returns an interceptor which attaches c to the context
  and request on enter."
  [c & args]
  (let [{:keys [name enter error leave]} (apply hash-map args)]
    (interceptor/interceptor {:name  (interceptor/interceptor-name name)
                              :enter (fn [ctx] (-> ctx
                                                   (assoc ::component c)
                                                   (assoc-in [:request ::component] c)))})))
