(ns clj.tr069.soap
  (:require (clojure.string))
  (:use (clj.tr069 databinding schema))
  (:import (org.apache.axiom.soap SOAPEnvelope
                                  SOAPHeaderBlock)
           (org.apache.axiom.om OMElement)
           (org.apache.axiom.soap.impl.builder StAXSOAPModelBuilder)
           (java.io InputStream)
           (javax.xml.stream XMLInputFactory)))

