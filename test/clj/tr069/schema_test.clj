(ns clj.tr069.schema-test
  (:use clojure.test
        clj.tr069.schema
        clj.tr069.databinding)
  (:import (clj.tr069.schema Inform
                             Device
                             DeviceId
                             ParameterValueStruct)))

(def test-inform-object
  {:device-id {:oui "09cafe"
               :product-class "TEST"
               :manufacturer "MOBICLOUD"
               :serial-number "0xcafebabe"}
   :parameter-list [{:name "InternetGatewayDevice.DeviceSummary"
                     :value {:type :string :value "test device"}}
                    {:name "InternetGatewayDevice.DeviceInfo.SpecVersion"
                     :value {:type :string :value "1.0a"}}
                    {:name "InternetGatewayDevice.DeviceInfo.HardwareVersion"
                     :value {:type :string :value "1.0"}}
                    {:name "InternetGatewayDevice.DeviceInfo.SoftwareVersion"
                     :value {:type :string :value "1.1"}}
                    {:name "InternetGatewayDevice.DeviceInfo.ProvisioningCode"
                     :value {:type :string :value "cloud"}}
                    {:name "InternetGatewayDevice.ManagementServer.ConnectionRequestURL"
                     :value {:type :string :value "http://201.101.101.101:1234"}}
                    {:name "InternetGatewayDevice.ManagementServer.ParameterKey"
                     :value {:type :string :value "4321"}}
                    {:name "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANPPPConnection.1.ExternalIPAddress"
                     :value {:type :string :value "201.101.101.101"}}]})

(deftest inform->device-success
  (testing "Failure: incorrect device object created from inform"
    (is (= (Device.
            "09cafe_0xcafebabe"
            "09cafe"
            "TEST"
            "0xcafebabe"
            "MOBICLOUD"
            "201.101.101.101"
            "http://201.101.101.101:1234"
            nil
            nil
            "4321"
            "cloud"
            "1.0a"
            "1.0"
            "1.1"
            "test device"
            "InternetGatewayDevice"
            "WANDevice.1.WANConnectionDevice.1.WANPPPConnection.1."
            )
           (inform->device test-inform-object)))))