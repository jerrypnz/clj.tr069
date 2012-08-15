(ns clj.tr069.soap
  (:require (clojure.string))
  (:use (clj.tr069 databinding schema))
  (:import (org.apache.axiom.soap SOAPEnvelope
                                  SOAPHeaderBlock)
           (org.apache.axiom.om OMElement)
           (org.apache.axiom.soap.impl.builder StAXSOAPModelBuilder)
           (java.io InputStream)
           (javax.xml.stream XMLInputFactory)))

(def ^:private ^XMLInputFactory xml-input-factory (XMLInputFactory/newFactory))

(defn- parse-envelope
  [^InputStream in]
  (let [builder (StAXSOAPModelBuilder.
                  (.createXMLStreamReader xml-input-factory in))]
    (.getSOAPEnvelope builder)))

(defn- get-body
  [^SOAPEnvelope envelope]
  (let [body (.getBody envelope)
        fault (.getFault body)]
    (if (.hasFault envelope)
      {:fault {:fault-code (.getText (.getCode fault)) 
               :fault-string (.getText (.getReason fault))
               :detail (bind-model (.getDetail fault))}}
      (bind-model (.getFirstElement body)))))

(defn- get-header
  [^SOAPEnvelope envelope]
  (let [header (.getHeader envelope)]
    (reduce (fn [hdr-map ^SOAPHeaderBlock hdr-blk]
              (assoc hdr-map (keyword (.getLocalName hdr-blk))
                     {:must-understand (.getMustUnderstand hdr-blk)
                      :name (.getLocalName hdr-blk)
                      :value (.getText hdr-blk)}))
            {}
            (iterator-seq (.extractAllHeaderBlocks header)))))

(defn parse
  [^InputStream in]
  (let [envelope (parse-envelope in)]
     {:body (get-body envelope)
      :header (get-header envelope)}))

