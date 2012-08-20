(ns clj.tr069.core
  (:use (clj.tr069 databinding schema)))

(defn tr069-handler [request]
  {:status 200
   :headers {"Content-Type" "text/xml; charset=UTF-8"}
   :body (serialize-tr069-message (create-tr069-message (->InformResponse 1)))
   })

