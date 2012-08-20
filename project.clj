(defproject clj.tr069 "0.1.0-SNAPSHOT"
  :description "TR-069 ACS"
  :url "http://jerrypeng.me"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-ring "0.7.1"]]
  :ring {:handler clj.tr069.core/handler
         :port 8080}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.apache.ws.commons.axiom/axiom-api "1.2.13"]
                 [org.apache.ws.commons.axiom/axiom-impl "1.2.13"]
                 ])
