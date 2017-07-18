(ns petstore.protocols
  (:refer-clojure :exclude [derive]))

(defprotocol AuthStore
  (user-identity [this username]
    "Retrieves a user by username"))

(defprotocol Authenticate
  (authenticate [this creds]
    "Returns a user entity if the :username and :password in creds are valid."))

(defprotocol Hasher
  (derive [this s]
    "Generates a hash from string s.")
  (check [this attempt encrypted]
    "Checks that attempt is a variant of encrypted."))
