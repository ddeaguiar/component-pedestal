(ns com.ddeaguiar.component-pedestal
  (:refer-clojure :exclude [ref])
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.log :as log]
            [io.pedestal.http.route :as route]
            [clojure.walk :as walk]
            [clj-fuzzy.jaro-winkler :refer [jaro-winkler]]))

(defrecord Interceptor [name enter error leave]
  interceptor/IntoInterceptor
  (-interceptor [this]
    (let [deps (dissoc this :name :enter :error :leave)]
      (interceptor/->Interceptor name
                                 (when enter (fn [ctx] (enter (assoc ctx ::deps deps))))
                                 (when leave (fn [ctx] (leave (assoc ctx ::deps deps))))
                                 (when error (fn [ctx] (error (assoc ctx ::deps deps))))))))

(defn component-interceptor
  "Interceptor ctor."
  [interceptor-map]
  (map->Interceptor interceptor-map))

(defn- var-get-if-bound
  [^clojure.lang.Var x]
  (when (and x (.isBound x))
    (var-get x)))

(defn- resolve-sym
  [sym]
  (if-let [resolved (some-> sym resolve var-get-if-bound)]
    resolved
    (throw (ex-info (format "Unable to resolve symbol '%s'." sym) {:reason ::unresolvable-symbol
                                                                   :cause  sym}))))

(defrecord Handler [handler]
  interceptor/IntoInterceptor
  (-interceptor [this]
    (let [f (resolve-sym handler)]
      (interceptor/map->Interceptor {:name  (interceptor/interceptor-name (keyword handler))
                                     :enter (fn [ctx]
                                              (let [req (-> ctx
                                                            :request
                                                            (assoc ::deps
                                                                   (dissoc this :handler)))]
                                                (assoc ctx :response (f req))))}))))

(defn component-handler
  "Handler ctor."
  [sym]
  {:pre [(symbol? sym)]}
  (Handler. sym))

(defn- dev?
  [service-map]
  (= :dev (:env service-map)))

(defn- test?
  [service-map]
  (= :test (:env service-map)))

;; Using deftype instead of defrecord because
;; I don't want Ref to be supported with 'into'.
;; A common pattern with route definitions is:
;;
;; `["/foo" :get (into common-interceptors [my-interceptor my-handler])]`
(deftype Ref [key])

(defn ref
  [key]
  {:pre [(keyword? key)]}
  (Ref. key))

(defn ref? [x] (instance? Ref x))

(defn- ref-alternative
  [r dict]
  (->> dict
       keys
       (map #(vector % (jaro-winkler (str (.key r)) (str %))))
       (sort-by last)
       last
       first))

(defn- resolve-ref
  "Resolves r using dict."
  [r dict]
  {:pre [(ref? r)]}
  (if-let [resolved (get dict (.key r))]
    resolved
    (throw (ex-info (format "Ref '%s' was not resolved. Did you mean '%s'?"
                            (.key r)
                            (ref-alternative r dict))
                    {:reason ::unresolvable-component-reference
                     :cause  (.key r)}))))

(defn resolve-refs
  "Resolves all References in data using dict."
  [data dict]
  (walk/postwalk (fn [x]
                   (cond-> x (ref? x) (resolve-ref dict)))
                 data))

(defprotocol Service
  (service-fn [this]
    "The service fn."))

(defrecord Pedestal [service server]
  component/Lifecycle
  (start [this]
    (if server
      this
      (do
        (log/info :msg "Starting Pedestal.")
        (assoc this
               :server
               (-> service
                   (update ::http/routes resolve-refs (dissoc this [:service]))
                   http/default-interceptors
                   (cond-> (dev? service) http/dev-interceptors)
                   http/create-server
                   (cond-> (not (test? service)) http/start))))))
  (stop [this]
    (when (and server (not (test? server)))
      (log/info :msg "Stopping Pedestal.")
      (try
        (http/stop service)
        (catch Throwable t
          (log/error :msg "Error stopping Pedestal." :exception t))))
    (assoc this :server nil))

  Service
  (service-fn [this]
    (get-in this [:server ::http/service-fn])))

(defn component-pedestal
  "Pedestal ctor."
  ([] (map->Pedestal {}))
  ([service] (map->Pedestal {:service service})))
