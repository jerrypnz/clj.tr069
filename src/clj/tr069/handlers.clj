(ns clj.tr069.handlers
  (:use (clj.tr069 databinding
                   schema
                   session))
  (:import (clj.tr069.schema Inform
                             InformResponse
                             TransferComplete
                             GetRPCMethods
                             GetRPCMethodsResponse
                             AddObject
                             DeleteObject
                             GetParameterValues
                             GetParameterNames
                             GetParameterAttributes
                             SetParameterValues
                             SetParameterAttributes
                             Download
                             Upload)))

(def tr069-msg-type-hierarchy
  (-> (make-hierarchy)
      (derive ::server-rpc ::rpc-request)
      (derive ::cpe-rpc    ::rpc-request)
      (derive Inform ::server-rpc)
      (derive TransferComplete ::server-rpc)
      (derive GetRPCMethods ::server-rpc)
      (derive GetRPCMethods ::cpe-rpc)
      (derive AddObject ::cpe-rpc)
      (derive DeleteObject ::cpe-rpc)
      (derive GetParameterValues ::cpe-rpc)
      (derive GetParameterNames  ::cpe-rpc)
      (derive GetParameterAttributes ::cpe-rpc)
      (derive SetParameterValues ::cpe-rpc)
      (derive SetParameterAttributes ::cpe-rpc)
      (derive Download ::cpe-rpc)
      (derive Upload ::cpe-rpc)))


(defmulti handle-tr069-message
  (fn [{:keys [tr-message tr-session]}]
    (let [state (:state tr-session)]
      (if-let [body (:body tr-message)]
        [state (type body)]
        [state ::empty-msg])))
  :default ::invalid-request
  :hierarchy #'tr069-msg-type-hierarchy)

(defmethod handle-tr069-message ::invalid-request
  [{:keys [tr-message tr-session]}]
  {:tr-session (mark-session-error tr-session)
   :tr-message (create-tr069-message
                (map->Fault {:fault-code 8003
                             :fault-string "Invalid argument"}))})

(defmethod handle-tr069-message [:initializing Inform]
  [{:keys [tr-message tr-session]}]
  (let [inform (:body tr-message)
        tr-session (establish tr-session inform)
        inform-resp (InformResponse. (:max-envelopes inform))]
    {:tr-message (create-tr069-message inform-resp)
     :tr-session tr-session}))

(defmethod handle-tr069-message [:established ::empty-msg]
  [{:keys [tr-session]}]
  {:tr-session (start-server-control tr-session)
   :tr-message (create-tr069-message (GetRPCMethods.)
                                     :ID (str (System/currentTimeMillis)))})

(defmethod handle-tr069-message [:server-control GetRPCMethodsResponse]
  [{:keys [tr-session]}]
  {:tr-session (end-session tr-session)
   :tr-message nil})