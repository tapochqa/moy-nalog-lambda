(ns moy-nalog-lambda.core 
  (:gen-class)
  (:require
    [moy-nalog-lambda.lambda   :as lambda]
    [clojure.string    :as str]
    [cheshire.core     :as json]))


(defn lambda
  [config]
  (-> (lambda/->request)
      (lambda/handle-request! config)
      (lambda/response->)))

(defn -main
  [login password]
  (lambda 
    {:login login
     :password password}))


(comment
  
  
  (binding [*in* (-> "modulbank-fixture.json"
                 clojure.java.io/resource
                 clojure.java.io/reader)]

    (-main 
      (slurp "login")
      (slurp "password"))))