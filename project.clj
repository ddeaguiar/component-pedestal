(defproject com.ddeaguiar/component-pedestal "0.1.0-SNAPSHOT"
  :description "A light-weight Pedestal component library."
  :url "https://github.com/ddeaguiar/component-pedestal"
  :license {:name "The MIT License"
            :url  "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.stuartsierra/component "0.3.2" :scope "provided"]
                 [io.pedestal/pedestal.service "0.5.2" :scope "provided"]
                 [clj-fuzzy "0.4.0"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [com.stuartsierra/component.repl "0.2.0"]
                                  [io.pedestal/pedestal.jetty "0.5.2"]
                                  [io.pedestal/pedestal.tomcat "0.5.2"]
                                  [io.pedestal/pedestal.immutant "0.5.2"]
                                  [ch.qos.logback/logback-classic "1.1.8" :exclusions [org.slf4j/slf4j-api]]
                                  [org.slf4j/jul-to-slf4j "1.7.22"]
                                  [org.slf4j/log4j-over-slf4j "1.7.22"]]
                   :source-paths ["dev"]}})
