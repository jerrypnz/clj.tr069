(ns clj.tr069.schema
  (:use (clj.tr069 datatype databinding util))
  (:require [clojure.string :as string]))

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

; Inform Response
(deftr069type
  ^:top-level
  InformResponse
  (max-envelopes :int :MaxEnvelopes))

(defrecord Device
    [identifier
     oui
     product-class
     serial-number
     manufacturer
     ip
     ^{:path "ManagementServer.ConnectionRequestURL"} conn-req-url
     ^{:path "ManagementServer.ConnectionRequestUsername"} conn-req-username
     ^{:path "ManagementServer.ConnectionRequestPassword"} conn-req-password
     ^{:path "ManagementServer.ParameterKey"} param-key
     ^{:path "DeviceInfo.ProvisioningCode"} provisioning-code
     ^{:path "DeviceInfo.SpecVersion"} spec-version
     ^{:path "DeviceInfo.HardwareVersion"} hardware-ver
     ^{:path "DeviceInfo.SoftwareVersion"} software-ver
     ^{:path "DeviceSummary"} device-summary
     root-obj-name
     wan-path])

(defn inform->device [inform]
  (let [dev-id (:device-id inform)
        field-mapping (reduce (fn [mapping field]
                                (if-let [path (:path (meta field))]
                                  (assoc mapping path (keyword field))
                                  mapping))
                              {}
                              (Device/getBasis))
        device-map (reduce (fn [results {:keys [name value]}]
                             (let [[root-name path] (string/split name #"\." 2)
                                   value (:value value)]
                              (if-let [field (field-mapping path)]
                                (-> results
                                    (assoc field value)
                                    (assoc :root-obj-name root-name))
                                (if-let [ip-path (match-external-ip path)]
                                  (-> results
                                      (assoc :ip value)
                                      (assoc :wan-path ip-path))
                                  results))))
                           (into {:identifier (str
                                               (:oui dev-id) "_"
                                               (:serial-number dev-id))}
                                 dev-id)
                          (:parameter-list inform))]
    (map->Device device-map)))
