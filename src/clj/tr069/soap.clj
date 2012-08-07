(ns clj.tr069.soap
  (:require (clojure.string))
  (:import (org.apache.axiom.soap SOAPEnvelope
                                  SOAPHeaderBlock)
           (org.apache.axiom.om OMElement)
           (org.apache.axiom.soap.impl.builder StAXSOAPModelBuilder)
           (java.io InputStream
                    FileInputStream)
           (javax.xml.namespace QName)
           (javax.xml.stream XMLInputFactory)))

(def ^:private ^XMLInputFactory xml-input-factory (XMLInputFactory/newFactory))

(def ^:private xml-ns-xsd "http://www.w3.org/2001/XMLSchema")
(def ^:private xml-ns-xsi "http://www.w3.org/2001/XMLSchema-instance")
(def ^:private xml-ns-soap "http://schemas.xmlsoap.org/soap/envelope/")
(def ^:private xml-ns-soapenc "http://schemas.xmlsoap.org/soap/encoding/")

(defn- parse-envelope
  [^InputStream in]
  (let [builder (StAXSOAPModelBuilder.
                  (.createXMLStreamReader xml-input-factory in))]
    (.getSOAPEnvelope builder)))

(defn- get-attr
  [^OMElement om local-name]
  (.getAttributeValue om (QName. local-name)))

(defn- do-databinding
  [^OMElement om]
  (let [name (.getLocalName om)
        text (.getText om)
        first-elem (.getFirstElement om)
        array-type (get-attr om "arrayType")]
    (cond
      (nil? first-elem)
        (if (clojure.string/blank? text) nil text)
      (nil? array-type)
        (reduce (fn [elem-map ^OMElement child]
                  (assoc elem-map (.getLocalName child) (do-databinding child)))
                {}
                (iterator-seq (.getChildElements om)))
      :else 
        (map do-databinding (iterator-seq (.getChildElements om))))))

(defn- get-body
  [^SOAPEnvelope envelope]
  (let [body (.getBody envelope)
        fault (.getFault body)]
    (if (.hasFault envelope)
      {:fault {:fault-code (.getText (.getCode fault)) 
               :fault-string (.getText (.getReason fault))
               :detail (do-databinding (.getDetail fault))}}
      (do-databinding body))))

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

