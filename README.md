# component-pedestal

[![Build Status](https://travis-ci.org/ddeaguiar/component-pedestal.svg?branch=master)](https://travis-ci.org/ddeaguiar/component-pedestal)

## Currently Experimental

A [Component](https://github.com/stuartsierra/component)
implementation of [Pedestal](https://github.com/pedestal/pedestal)
 whose goal is to streamline the wiring of system dependencies to
interceptors and handlers.

## Usage

Components provide service capabilities by exposing routes. To do this
they implement the `RouteProvider` protocol. The `Pedestal` component
is configured in the system map to depend on route providers and
builds the service's routes from them.

Interceptor and/or handlers are created in the context of a
`RouterProvider` component so that they can access it's state.

```
(require '[io.pedestal.http :as http])
(require '[io.pedestal.http.body-params :as body-params])
(require '[com.ddeaguiar.component.pedestal :as component-pedestal])
(require '[com.stuartsierra.component :as component])
(require '[ring-resp/response :as ring-resp])

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
                         (component-pedestal/component-pedestal)
                        [:user-store])))
```

## Copyright and License

MIT License

Copyright (c) 2017 Daniel De Aguiar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
