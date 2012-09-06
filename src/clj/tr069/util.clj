(ns clj.tr069.util)

(def ^:private external-ip-regx
  #"^(WANDevice\.\d+\.WANConnectionDevice\.\d+\.WAN[a-zA-Z]+Connection\.\d+\.)ExternalIPAddress$")

(defn match-external-ip
  "Match external IP address parameter in Inform"
  [param-name]
  (-> (re-seq external-ip-regx param-name)
      first
      second))