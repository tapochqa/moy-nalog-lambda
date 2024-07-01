;; https://github.com/ring-clojure/ring/blob/master/SPEC
;; https://cloud.yandex.ru/docs/functions/concepts/function-invoke

(ns moy-nalog-lambda.lambda
  
  (:require
   [clojure.java.io :as io]
   [cheshire.core :as json]
   [clojure.string :as str]
   [moy-nalog-lambda.handling :as handling])
  
  (:import
   java.io.File
   java.io.InputStream
   java.util.Base64
   ))

(defn request-to-keywords [req]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" req)]
  [(keyword k) v])))


(defn str->bytes
  ^bytes [^String string ^String encoding]
  (.getBytes string encoding))


(defn b64-decode
  [^bytes encoded]
  (.decode (Base64/getDecoder) encoded))


(defn parse-request
  [{:strs [requestContext
           path
           queryStringParameters
           httpMethod
           body
           isBase64Encoded
           headers] :as request}]
  (let [parsed
        {:remote-addr (get-in requestContext ["identity" "sourceIp"])
         :uri (if (= path "") "/" path)
         :query-params queryStringParameters
         :request-method 
         (if httpMethod
           (-> httpMethod name str/lower-case keyword)
           :trigger)
         :headers (update-keys headers str/lower-case)
         :body (if isBase64Encoded
                 (-> (str "" body)
                     (str->bytes "UTF-8")
                     (b64-decode)
                     (io/input-stream))
                 (-> (str "" body)
                     (str->bytes "UTF-8")
                     (io/input-stream)))}]
    parsed))


(defn ->request []
  (-> *in*
      (json/parse-stream)
      (parse-request)))


(defn encode-body [body]
  (cond

    (string? body)
    {:body body
     :isBase64Encoded false}

    :else
    (throw (ex-info "Wrong response body"
                    {:body body}))))


(defn handle-request!
  [{:keys [headers query-params body] :as request} config]
  
  (let [params (into {} 
                  (for [[k v] query-params] 
                    [(keyword k) v]))]
  
    {:body
     (json/encode
       (handling/the-handler
         config
         params))
     
     :headers headers
     
     :status 200}))


(defn response->
  [{:keys [status headers body]}]
  (json/with-writer [*out* nil]
    (json/write
     (cond-> nil
       status
       (assoc :statusCode status)
       headers
       (assoc :headers headers)
       body
       (assoc :body body)))))
