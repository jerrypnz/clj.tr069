(ns clj.tr069.schema
  (:use (clj.tr069 datatype databinding)))

; Inform
(deftr069type
  Inform
  (device-id      :child :DeviceId)
  (events         :array :Event         :EventStruct)
  (parameter-list :array :ParameterList :ParameterValueStruct)
  (retry-count    :value :RetryCount    :int)
  (current-time   :value :CurrentTime   :dateTime)
  (max-envelopes  :value :MaxEnvelopes  :int))

(deftr069type
  EventStruct
  (command-key :value :CommandKey :string)
  (event-code  :value :EventCode  :string))

(deftr069type
  ParameterValueStruct
  (name  :value :Name :string)
  (value :any-simple-value :Value))

(deftr069type
  DeviceId
  (manufacturer :value :Manufacturer :string)
  (oui          :value :OUI          :string)
  (product-class :value :ProductClass :string)
  (serial-number :value :SerialNumber :string))

