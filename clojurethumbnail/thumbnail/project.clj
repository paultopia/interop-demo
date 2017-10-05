(defproject thumbnail "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.imgscalr/imgscalr-lib "4.2"]]
  :main ^:skip-aot thumbnail.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
