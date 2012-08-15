(ns clj.tr069.datatype
  (:import (javax.xml.bind DatatypeConverter)))

; XSD datatype handling
(defmulti parse-value
  "Parse an XSD string to a value according to its type"
  (fn [type value] (keyword type)))

(defmethod parse-value nil
  [type value]
  value)

(defmethod parse-value :string
  [type value]
  (DatatypeConverter/parseString value))

(defmethod parse-value :int
  [type value]
  (DatatypeConverter/parseInt value))

(defmethod parse-value :unsignedInt
  [type value]
  (DatatypeConverter/parseUnsignedInt value))

(defmethod parse-value :boolean
  [type value]
  (DatatypeConverter/parseBoolean value))

(defmethod parse-value :base64
  [type value]
  (DatatypeConverter/parseBase64Binary value))

(defmethod parse-value :dateTime
  [type value]
  (DatatypeConverter/parseDateTime value))

(defmulti print-value
  "Print a value to an XSD string according to its type"
  (fn [type value] (keyword type)))

(defmethod print-value nil
  [type value]
  (.toString value))

(defmethod print-value :string
  [type value]
  (DatatypeConverter/printString value))

(defmethod print-value :int
  [type value]
  (DatatypeConverter/printInt value))

(defmethod print-value :unsignedInt
  [type value]
  (DatatypeConverter/printUnsignedInt value))

(defmethod print-value :boolean
  [type value]
  (DatatypeConverter/printBoolean value))

(defmethod print-value :base64
  [type value]
  (DatatypeConverter/printBase64Binary value))

(defmethod print-value :dateTime
  [type value]
  (DatatypeConverter/printDateTime value))
