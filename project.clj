(defproject bidi-swagger "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [bidi "1.20.0"]]
  :profiles {:dev {:dependencies [[aprint "0.1.3"]
                                  [ring/ring-json "0.3.1"]
                                  [metosin/ring-swagger-ui "2.1.0"]]}}
  :plugins [[lein-ring "0.9.4"]]
  :ring {:init           bidi-swagger.monster-demo/init
         :handler        bidi-swagger.monster-demo/bidi-handler})
