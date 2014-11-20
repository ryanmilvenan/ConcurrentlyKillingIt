(defproject muzak "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.2.0"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-json "0.2.0"]
                 [enfocus "2.1.0"]]
  :resource-paths ["test/cisd-jhdf5.jar" "test/cisd-jhdf5-core.jar" "test/cisd-jhdf5-tools.jar"]
  :plugins [[lein-ring "0.8.13"]
            [lein-cljsbuild "1.0.3"]]
  :cljsbuild {
    :builds [{
      :sourth-paths ["src-cljs"]
      :compiler {
        :output-to "resources/public/js/main.js"
        :optimizations :whitespace
        :pretty-print true}}]}
  :ring {:handler muzak.core.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
