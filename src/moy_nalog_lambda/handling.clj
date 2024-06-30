(ns moy-nalog-lambda.handling
  (:require
    [clojure.string :as str]
    [moy-nalog.core :as moy-nalog]
    [cheshire.core :as json]))

(defn parse-sum
    [email]
    (let
      [group (re-find #"на\s*[-+]?[0-9]*(\.[0-9]+)*RUB" email)
       needed (first group)
       cut (-> needed
             (str/replace #"RUB" "")
             (str/replace #"на " ""))]
      (parse-double cut)))


(defn the-handler
  [config body]
  
  (let [amount (parse-double (:amount body))]
    
    (moy-nalog/add-income config "Услуги музыкального продюсирования" amount)))