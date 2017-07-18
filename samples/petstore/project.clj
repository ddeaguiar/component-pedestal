(defproject petstore "0.0.1-SNAPSHOT"
  :description "Petstore sample built using Component-Pedestal."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 ;; When using clojure 1.9.0-alpha15+ the following is
                 ;; required until 0.5.3 lands.
                 ;; See https://github.com/pedestal/pedestal/issues/505
                 [org.clojure/core.async "0.3.443"]
                 [io.pedestal/pedestal.service "0.5.2" :exclusions [org.clojure/core.async]]

                 [io.pedestal/pedestal.jetty "0.5.2"]
                 [ch.qos.logback/logback-classic "1.1.8" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.22"]
                 [org.slf4j/jcl-over-slf4j "1.7.22"]
                 [org.slf4j/log4j-over-slf4j "1.7.22"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.datomic/datomic-free "0.9.5561.50"
                  :exclusions [org.slf4j/slf4j-api
                               org.slf4j/slf4j-nop
                               org.slf4j/slf4j-log4j12
                               org.slf4j/log4j-over-slf4j
                               org.slf4j/jcl-over-slf4j
                               org.slf4j/jul-to-slf4j
                               org.clojure/clojure]]
                 [io.rkn/conformity "0.5.0"]
                 [buddy/buddy-auth "1.4.1" :exclusions [cheshire]]
                 [buddy/buddy-hashers "1.2.0"]
                 [com.ddeaguiar/component-pedestal "0.1.0-SNAPSHOT"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  ;; If you use HTTP/2 or ALPN, use the java-agent to pull in the correct alpn-boot dependency
  ;:java-agents [[org.mortbay.jetty.alpn/jetty-alpn-agent "2.0.5"]]
  :profiles {:dev {:source-paths ["dev/src"]
                   :aliases {"run-dev" ["trampoline" "run" "-m" "com.stuartsierra.component.user-helpers/go"]}
                   :dependencies [[com.stuartsierra/component.repl "0.2.0" :exclusions [com.stuartsierra/component]]]
                   :repl-options {:init-ns user
                                  :welcome (println "Type (go) to start the system.")}}
             :uberjar {:aot [petstore.server]}}
  :main ^{:skip-aot true} petstore.server)
