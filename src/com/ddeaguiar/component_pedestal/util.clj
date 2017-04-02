(ns com.ddeaguiar.component-pedestal.util
  (:import (org.apache.commons.text.similarity JaroWinklerDistance)))

(defn jaro-winkler
  "Computes the similarity between s1 and s2 and returns a value that
     lies in the interval [0.0, 1.0]."
  [s1 s2]
  {:pre [(and (string? s1) (string? s2))]}
  (.apply (JaroWinklerDistance.) s1 s2))
