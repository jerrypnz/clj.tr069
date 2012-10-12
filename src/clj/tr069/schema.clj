(ns clj.tr069.schema
  (:use (clj.tr069 datatype databinding util))
  (:require [clojure.string :as string]))

;; Fault struct
(deftr069type FaultStruct
  (fault-code   :int    :FaultCode) 
  (fault-string :string :FaultString))

;; Fault
(deftr069type ^:top-level Fault
  (fault-code   :int    :FaultCode) 
  (fault-string :string :FaultString)
  (details      :inline-array :SetParameterValuesFault))

(deftr069type SetParameterValuesFault
  (fault-code   :int    :FaultCode :int) 
  (fault-string :string :FaultString))

;; Inform
(deftr069type ^:top-level Inform
  (device-id      :child       :DeviceId)
  (events         :child-array :Event         :EventStruct)
  (parameter-list :child-array :ParameterList :ParameterValueStruct)
  (retry-count    :int         :RetryCount)
  (current-time   :dateTime    :CurrentTime)
  (max-envelopes  :int         :MaxEnvelopes))

(deftr069type EventStruct
  (command-key :string :CommandKey :string)
  (event-code  :string :EventCode  :string))

(deftr069type ParameterValueStruct
  (name  :string :Name :string)
  (value :any-simple-value :Value))

(deftr069type DeviceId
  (manufacturer :string :Manufacturer)
  (oui          :string :OUI)
  (product-class :string :ProductClass)
  (serial-number :string :SerialNumber))

;; Inform Response
(deftr069type ^:top-level InformResponse
  (max-envelopes :int :MaxEnvelopes))

;; Transfer Complete
(deftr069type ^:top-level TransferComplete
  (command-key :string :CommandKey)
  (fault :child :FaultStruct)
  (start-time    :dateTime :StartTime)
  (complete-time :dateTime :CompleteTime))

(deftr069type ^:top-level TransferCompleteResponse)

;; Get RPC Methods
(deftr069type ^:top-level GetRPCMethods)

;; Get RPC Methods Response
(deftr069type ^:top-level GetRPCMethodsResponse
  (method-list :string-array :MethodList))

;; Get Parameter Values
(deftr069type ^:top-level GetParameterValues
  (parameter-names :string-array :ParameterNames))

;; Get Parameter Values Response
(deftr069type ^:top-level GetParameterValuesResponse
  (parameter-list :child-array :ParameterList :ParameterValueStruct))

;; Get Parameter Names
(deftr069type ^:top-level GetParameterNames
  (parameter-path :string  :ParameterPath)
  (next-level     :boolean :NextLevel))

;; Get Parameter Names Response
(deftr069type ^:top-level GetParameterNamesResponse
  (parameter-list :child-array :ParameterList :ParameterInfoStruct))

(deftr069type ParameterInfoStruct
  (name     :string  :Name)
  (writable :boolean :Writable))

;; Get Parameter Attributes
(deftr069type ^:top-level GetParameterAttributes
  (parameter-names :string-array :ParameterNames))

;; Get Parameter Attributes Response
(deftr069type ^:top-level GetParameterAttributesResponse
  (parameter-list :child-array :ParameterAttributeStruct))

(deftr069type ParameterAttributeStruct
  (name :string :Name)
  (notification :int :Notification)
  (access-list :string-array :AccessList))

;; Set Parameter Values
(deftr069type ^:top-level SetParameterValues
  (parameter-list :child-array :ParameterValueStruct))

;; Set Parameter Values Response
(deftr069type ^:top-level SetParameterValuesResponse
  (status :int :Status))

;; Set Parameter Attributes
(deftr069type ^:top-level SetParameterAttributes
  (parameter-list :child-array :SetParameterAttributesStruct))

(deftr069type SetParameterAttributesStruct
  (name :string :Name)
  (notification-change :boolean :NotificationChange)
  (notification :int :Notification)
  (access-list-change :boolean :AccessListChange)
  (access-list :string-array :AccessList))

;; Set Parameter Attributes Response
(deftr069type ^:top-level SetParameterAttributesResponse)

;; Add Object
(deftr069type ^:top-level AddObject
  (object-name :string :ObjectName)
  (parameter-key :string :ParameterKey))

;; Add Object Response
(deftr069type ^:top-level AddObjectResponse
  (instance-number :unsignedInt :InstanceNumber)
  (status :int :Status))

;; Delete Object
(deftr069type ^:top-level DeleteObject
  (object-name :string :ObjectName)
  (parameter-key :string :ParameterKey))

;; Delete Object Response
(deftr069type ^:top-level DeleteObjectResponse
  (status :int :Status))

;; Download
(deftr069type ^:top-level Download
  (command-key :string :CommandKey)
  (file-type   :string :FileType)
  (url         :string :URL)
  (username    :string :Username)
  (password    :string :Password)
  (file-size   :unsignedInt :FileSize)
  (target-file-name :string :TargetFileName)
  (delay-seconds :unsignedInt :DelaySeconds)
  (success-url :string :SuccessURL)
  (failure-url :string :FailureURL))

;; Download Response
(deftr069type ^:top-level DownloadResponse
  (status :int :Status)
  (start-time :dateTime :StartTime)
  (complete-time :dateTime :CompleteTime))

;; Upload
(deftr069type ^:top-level Upload
  (command-key :string :CommandKey)
  (file-type   :string :FileType)
  (url         :string :URL)
  (username    :string :Username)
  (password    :string :Password)
  (delay-seconds :unsignedInt :DelaySeconds))

;; Upload Response
(deftr069type ^:top-level UploadResponse
  (status :int :Status)
  (start-time :dateTime :StartTime)
  (complete-time :dateTime :CompleteTime))

;; Factory Reset
(deftr069type ^:top-level FactoryReset)

(deftr069type ^:top-level FactoryResetResponse)


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
        field-mapping (->> (Device/getBasis)
                           (map #(if-let [path (:path (meta %))]
                                   [path (keyword %)]))
                           (remove nil?)
                           (into {}))
        device-map (->> (:parameter-list inform)
                        (mapcat (fn [{name :name {value :value} :value}]
                                  (let [[root-name path]
                                        (string/split name #"\." 2)]
                                    (if-let [field (field-mapping path)]
                                      {field value
                                       :root-obj-name root-name}
                                      (when-let [ip-path (match-external-ip path)]
                                        {:ip value
                                         :wan-path ip-path})))))
                        (remove nil?)
                        (into {:identifier (str
                                            (:oui dev-id) "_"
                                            (:serial-number dev-id))})
                        (into dev-id))]
    (map->Device device-map)))
