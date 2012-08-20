(ns clj.tr069.core
  (:use (clj.tr069 databinding schema))
  (:import (java.io PushbackInputStream)))

(defn- wrap-tr069-method [handler]
  (fn [{method :request-method :as request}]
    (cond
      (= method :post) (handler request)
      (= method :get) {:status 200
                       :headers {"Content-Type" "text/plain"}
                       :body "ACS is running"}
      :else {:status 405})))

(defn- wrap-tr069-exception [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        {:status 400
         :headers {"Content-Type" "text/plain"}
         :body (or (.getMessage e) "Bad request")}))))

(defn- parse-message-or-nil
  "Parse TR-069 message, or return nil if the content
  is empty"
  [body]
  (let [in (PushbackInputStream. body)
        first-byte (.read in)]
    (if (not= -1 first-byte)
      (do (.unread in first-byte)
          (parse-tr069-message in))
      nil)))

(defn- wrap-tr069-message [handler]
  (fn [{body :body :as request}]
    (let [req-tr-msg (parse-message-or-nil body)
          response (handler (assoc request :tr-message req-tr-msg))
          resp-tr-msg (:tr-message response)]
      (if (nil? resp-tr-msg)
        (assoc-in response [:headers "Content-Length"] "0")
        (let [resp-body (serialize-tr069-message resp-tr-msg)]
          (-> response
            (assoc-in [:headers "Content-Type"] "text/xml;charset=UTF-8")
            (assoc-in [:headers "Content-Length"] (str (.length resp-body)))
            (assoc :body resp-body)))))))

(defn- cwmp-handler
  "Core handler of TR-069 ACS"
  [{message :tr-message :as request}]
  {:status 200
   :tr-message message})

(def handler
  (-> cwmp-handler
    wrap-tr069-message
    wrap-tr069-exception
    wrap-tr069-method))

