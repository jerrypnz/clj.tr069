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

(defn establish
  [session inform]
  (if (= :initializing (:state session))
    (merge session
           {:state :established
            :device (inform->device inform)
            :inform inform})
    (throw (IllegalStateException.
            "Session state must be :initializing to establish"))))

(defn start-server-control
  [session]
  (if (= :established (:state session))
    (assoc session :state :server-control)
    (throw (IllegalStateException.
            "Session state must be :established to start server control"))))

(defn end-session
  [session]
  (assoc session :state :ended))

(defn mark-session-error
  [session]
  (assoc session :state :error))

(defn should-close?
  [session]
  (let [state (:state session)]
    (or (= state :ended)
        (= state :error))))