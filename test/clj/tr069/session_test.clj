(ns clj.tr069.session-test
  (:use clojure.test
        clj.tr069.session
        clj.tr069.schema
        clj.tr069.schema-test)
  (:import (clj.tr069.schema Inform
                             DeviceId
                             ParameterValueStruct)))

(deftest new-tr069-session-test-remote-addr
  (testing "Test failure: should use :remote-addr in request map"
    (is (=
         {:state :initializing
          :remote-addr "172.16.0.111"}
         (new-tr069-session {:remote-addr "172.16.0.111"})))))

(deftest new-tr069-session-test-x-forward-for
  (testing "Test failure: should use X-Forwarded-For header to get remote address"
    (is (=
         {:state :initializing
          :remote-addr "202.101.101.101"}
         (new-tr069-session {:remote-addr "172.16.0.111"
                             :headers {"X-Forwarded-For" "202.101.101.101"}})))))

(def test-device-object (inform->device test-inform-object))

(deftest establish-session-success
  (testing "Test failure: session could not be established corretly"
    (is (=
         {:state :established
          :remote-addr "202.101.101.101"
          :device test-device-object
          :inform test-inform-object}
         (establish-session {:state :initializing
                             :remote-addr "202.101.101.101"}
                            test-inform-object)))))

(deftest establish-session-incorrect-state
  (testing "Test failure: incorrect session state before establishing"
    (is (thrown? IllegalStateException
                 (establish-session {:state :established
                                     :remote-addr "202.101.101.101"}
                                    test-inform-object)))))