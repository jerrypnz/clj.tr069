(ns clj.tr069.session
  (:use (clj.tr069 schema))
  (:import (clj.tr069.schema Inform)))

(defn new-tr069-session
  "Create a new TR-069 session"
  [request]
  {:state :initializing
   :remote-addr (if-let [real-ip (get-in request [:headers "X-Forwarded-For"])]
                  real-ip
                  (:remote-addr request))})

(defn establish-session
  [session inform]
  (if (= :initializing (:state session))
    (merge session
                {:state :established
                 :device (inform->device inform)
                 :inform inform})
    (throw (IllegalStateException.
            "Session state must be :initializing to establish"))))