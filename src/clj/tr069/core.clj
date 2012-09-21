(ns clj.tr069.core
  (:use (clj.tr069 databinding schema session handlers)
        (ring.middleware session))
  (:require [clojure.string :as string])
  (:import (java.io PushbackInputStream
                    ByteArrayOutputStream
                    PrintStream)))


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
         :body (let [stream (ByteArrayOutputStream.)
                     printer (PrintStream. stream)
                     _ (.printStackTrace e printer)
                     msg (.toString stream)]
                 msg)}))))

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
          resp-tr-msg (:tr-message response)
          response (-> response
                       (dissoc :tr-message)
                       (assoc :status 200))]
      (if (nil? resp-tr-msg)
        (assoc-in response [:headers "Content-Length"] "0")
        (let [resp-body (serialize-tr069-message resp-tr-msg)]
          (-> response
            (assoc-in [:headers "Content-Type"] "text/xml;charset=UTF-8")
            (assoc-in [:headers "Content-Length"] (str (.length resp-body)))
            (assoc :body resp-body)))))))

(defn- get-or-create-tr069-session [request]
  (if-let [tr-session (:tr-session (:session request))]
    tr-session
    (new-tr069-session request)))

(defn- wrap-tr069-session [handler]
  (fn [request]
    (let [session (get-or-create-tr069-session request)
          response (handler (assoc request :tr-session session))
          session (:tr-session response)
          response (dissoc response :tr-session)]
      (if (should-close? session)
        (dissoc response :session)
        (assoc-in response [:session :tr-session] session)))))

(defn- cwmp-dispatcher
  "Core handler of TR-069 ACS"
  [{message :tr-message
    session :tr-session
    :as request}]
  (let [response (handle-tr069-message request)]
    (assoc response :status 200)))

(def handler
  (-> cwmp-dispatcher
      wrap-tr069-session
      wrap-tr069-message
      wrap-tr069-exception
      wrap-tr069-method
      (wrap-session {:cookie-name "tr069-session-id"
                     :cookie-attrs {:discard true
                                    :version 1}})))

