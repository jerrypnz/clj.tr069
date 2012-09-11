(ns clj.tr069.core-test
  (:use clojure.test
        clj.tr069.test-util
        clj.tr069.core))

(with-private-fns [clj.tr069.core [wrap-tr069-session]]
  (deftest wrap-tr069-session-keep-session
   (testing "Session should be present in the response object"
     (is (= {:tr-session {:state :established
                          :remote-addr "202.101.101.101"}}
            (let [handler-fn (fn [{s :tr-session}]
                               {:tr-session
                                (assoc s
                                  :state
                                  :established)})
                  handler (wrap-tr069-session handler-fn)
                  session {:tr-session
                           {:state :initializing
                            :remote-addr "202.101.101.101"}}
                  request {:session session}
                  response (handler request)]
              (:session response))))))

  (deftest wrap-tr069-session-new-session
    (testing "New session should be created"
      (let [handler (wrap-tr069-session identity)
            request {:remote-addr "202.101.101.101"}
            response (handler request)]
        (is (= {:tr-session {:state :initializing
                             :remote-addr "202.101.101.101"}}
               (:session response))))))

  (deftest wrap-tr069-session-delete-session
    (testing "Session should not be present in the response object"
      (is (every? nil?
                  (map (fn [state]
                         (let [handler-fn #(assoc-in
                                            % [:tr-session :state] state)
                               handler (wrap-tr069-session handler-fn)
                               session {:tr-session
                                        {:state :established
                                         :remote-addr "202.101.101.101"}}
                               request {:session session}
                               response (handler request)]
                           (:session response)))
                       [:ended :error]))))))