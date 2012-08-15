(ns clj.tr069.schema
  (:use (clj.tr069 datatype databinding)))

; Fault
(deftr069type
  ^:top-level
  Fault
  (fault-code   :int    :FaultCode) 
  (fault-string :string :FaultString)
  (details      :inline-array :SetParameterValuesFault))

(deftr069type
  SetParameterValuesFault
  (fault-code   :int    :FaultCode :int) 
  (fault-string :string :FaultString))

; Inform
(deftr069type
  ^:top-level
  Inform
  (device-id      :child       :DeviceId)
  (events         :child-array :Event         :EventStruct)
  (parameter-list :child-array :ParameterList :ParameterValueStruct)
  (retry-count    :int         :RetryCount)
  (current-time   :dateTime    :CurrentTime)
  (max-envelopes  :int         :MaxEnvelopes))

(deftr069type
  EventStruct
  (command-key :string :CommandKey :string)
  (event-code  :string :EventCode  :string))

(deftr069type
  ParameterValueStruct
  (name  :string :Name :string)
  (value :any-simple-value :Value))

(deftr069type
  DeviceId
  (manufacturer :string :Manufacturer)
  (oui          :string :OUI)
  (product-class :string :ProductClass)
  (serial-number :string :SerialNumber))

