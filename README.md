# component-pedestal

## Currently Experimental

A [Component](https://github.com/stuartsierra/component)
implementation of [Pedestal](https://github.com/pedestal/pedestal)
inspired by [Arachne](http://arachne-framework.org/) and
[Integrant](https://github.com/weavejester/integrant) whose
goal is to streamline the wiring of system dependencies to
interceptors and handlers.

## Usage

Interceptors and/or handlers which have system dependencies are
initialized in the system map using the `component-interceptor` and
`component-handler` constructor functions. These components are then
referenced in the routes using the `refs` function. Interceptor and/or
handler component references are resolved during service
initialization.

```
(require '[io.pedestal.http :as http])
(require '[io.pedestal.http.body-params :as body-params])
(require '[component.pedestal :as cp])
(require '[com.stuartsierra.component :as component])
(require '[ring-resp/response :as ring-resp])

(defn user
  [req]
  (let [db        (:db req)
        id        (get-in req [:path-params :id])
        user-name (get db id "not found")]
    (ring-resp/response (str "User name is: " user-name))))

(def common-interceptors [(body-params/body-params) http/html-body])

(def routes
#{["/user/:id" :get (conj common-interceptors (cp/ref :user-handler))]})

(def system (component/system-map
             :db {"dan"  "Daniel De Aguiar"
                  "yogi" "Yogi Bear"}
             :user-handler (component/using
                            (cp/component-handler `user)
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
                         :user-handler])))
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
