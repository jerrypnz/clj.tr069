(ns clj.tr069.session-test
  (:use clojure.test
        clj.tr069.session
        clj.tr069.schema
        clj.tr069.schema-test)
  (:import (clj.tr069.schema Inform
                             DeviceId
                             ParameterValueStruct)))

(deftest new-tr069-session-test-remote-addr
  (testing
      "Test failure: should use :remote-addr in request map"
    (is (=
         {:state :initializing
          :remote-addr "172.16.0.111"}
         (new-tr069-session {:remote-addr "172.16.0.111"})))))

(deftest new-tr069-session-test-x-forward-for
  (testing
      "Test failure: should use X-Forwarded-For header to get remote address"
    (is (=
         {:state :initializing
          :remote-addr "202.101.101.101"}
         (new-tr069-session {:remote-addr "172.16.0.111"
                             :headers {"X-Forwarded-For"
                                       "202.101.101.101"}})))))

(def test-device-object (inform->device test-inform-object))

(deftest establish-session-success
  (testing
      "Test failure: session could not be established corretly"
    (is (=
         {:state :established
          :remote-addr "202.101.101.101"
          :device test-device-object
          :inform test-inform-object}
         (establish {:state :initializing
                     :remote-addr "202.101.101.101"}
                    test-inform-object)))))

(deftest establish-session-incorrect-state
  (testing
      "Test failure: incorrect session state before establishing"
    (is (thrown? IllegalStateException
                 (establish {:state :established
                             :remote-addr "202.101.101.101"}
                            test-inform-object)))))

(deftest start-server-control-success
  (testing
      "Test failure: error start server control"
    (is (=
         {:state :server-control
          :remote-addr "202.101.101.101"
          :device test-device-object
          :inform test-inform-object}
         (-> {:state :initializing
              :remote-addr "202.101.101.101"}
             (establish test-inform-object)
             start-server-control)))))

(deftest start-server-control-incorrect-state
  (testing
      "Test failure: expected exception is not thrown"
    (is (thrown? IllegalStateException
                 (start-server-control
                  {:state :initializing
                   :remote-addr "202.101.101.101"})))))

(deftest end-session-success
  (testing
      "Test failure: incorrect sessoin state"
    (is (= :ended
           (-> {:state :initializing
                :remote-addr "202.101.101.101"}
               end-session
               :state)))))

(deftest mark-session-error-success
  (testing
      "Test failure: incorrect session state"
    (is (= :error
           (-> {:state :established
                :remote-addr "202.101.101.101"}
               mark-session-error
               :state)))))

(deftest ended-session-should-close?
  (testing
      "Test failure: ened session should be closed!"
    (is (should-close?
         {:state :ended}))))

(deftest error-session-should-close?
  (testing
      "Test failure: session with error should be closed!"
    (is (should-close?
         {:state :error}))))