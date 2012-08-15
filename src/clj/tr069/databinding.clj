(ns clj.tr069.databinding
  (:require (clojure.string))
  (:use (clj.tr069 datatype))
  (:import (org.apache.axiom.om OMElement)
           (javax.xml.namespace QName)))

(def ^:private xml-ns-xsd "http://www.w3.org/2001/XMLSchema")
(def ^:private xml-ns-xsi "http://www.w3.org/2001/XMLSchema-instance")
(def ^:private xml-ns-soap "http://schemas.xmlsoap.org/soap/envelope/")
(def ^:private xml-ns-soapenc "http://schemas.xmlsoap.org/soap/encoding/")

(def xsi-type (QName. xml-ns-xsi "type"))

; TR-069 Databinding protocol
(defprotocol TR069Databinding
  "TR-069 databinding protocol"
  (to-slurp [this] "Serialize the object to XML"))

(defrecord TypedValue
  [type value]
  TR069Databinding
  (to-slurp [this]
    [:Value 
     (if (nil? type)
       {}
       {:xsi:type (str "xsd:" type)})
     value]))

; Parsing methods
(defmulti bind-model
  "Do databinding"
  (fn [^OMElement om] (keyword (.getLocalName om))))

; Helper functions
(defn xml-string [coll]
  (let [elems (partition 3 coll)]
    (apply str
      (map (fn [[tag attrs body]]
             (str "<" (name tag)
                  (clojure.string/join
                    " " (cons "" (for [[k v] attrs] (str (name k) "=\"" v "\""))))
                  ">"
                  (if (coll? body) (str "\n" (xml-string body)) (str body))
                  "</" (name tag) ">\n"
                  ))
           elems))))

(defn parse-type [xsd-type]
  (clojure.string/replace xsd-type "xsd:" ""))

(defn array-type [type coll]
  {:soapenc:arrayType (str (name type) "[" (count coll) "]")})

; Macros for OMElement operations
(defmacro qname [local-name]
  `(QName. (name ~local-name)))

(defmacro first-elem
  ([om]
   `(.getFirstElement ~om))
  ([om local-name]
   `(.getFirstChildWithName ~om (qname ~local-name))))

(defmacro text [om local-name]
  `(.getText (.getFirstChildWithName ~om (qname ~local-name))))

(defmacro child-array-seq [om local-name]
  `(iterator-seq
     (.getChildElements (.getFirstChildWithName ~om (qname ~local-name)))))

(defmacro xml-binding [om cls & more]
  `(new ~cls
        ~@(map (fn [[type tag :as form]]
                 (case type
                   :child `(bind-model (first-elem ~om ~tag))
                   :array `(map bind-model (child-array-seq ~om ~tag))
                   :value `(parse-value ~(last form) (text ~om ~tag))
                   :any-simple-value (let [om-sym (gensym) type-sym (gensym)] 
                                       `(let [~om-sym
                                              (.getFirstChildWithName ~om (qname ~tag))
                                              ~type-sym 
                                              (parse-type (.getAttributeValue ~om-sym
                                                                              xsi-type))]
                                          (TypedValue. ~type-sym 
                                                       (parse-value
                                                         ~type-sym
                                                         (.getText ~om-sym)))))))
               more)))



(defmacro deftr069type [cls & more]
  `(do
     (defrecord ~cls
       [~@(map first more)]
       TR069Databinding
       (to-slurp [this]
         [~(keyword (str "cwmp:" (name cls))) {}
          (concat
            ~@(map (fn [[field type tag :as form]]
                     (case type
                       :child `(to-slurp ~field)
                       :array `[~tag (array-type (keyword (str "cwmp:" ~(name (last form))))
                                                 ~field)
                                (mapcat to-slurp ~field)]
                       :value `[~tag {} (print-value ~(last form) ~field)]
                       :any-simple-value `(to-slurp ~field))
                     )
                   more)
            )
          ]))
     (defmethod bind-model ~(keyword cls)
       [^OMElement ~(symbol "om")]
       (xml-binding ~(symbol "om") ~cls
         ~@(map rest more)))))

