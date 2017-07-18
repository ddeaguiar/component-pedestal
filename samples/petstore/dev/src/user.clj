(ns user
  (:require
   [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
   [com.stuartsierra.component.user-helpers :refer [dev go reset]]))

(set-refresh-dirs "dev/src" "src" "test")
