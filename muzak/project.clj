(defproject muzak "0.1.0-SNAPSHOT"
  :description "A visualizer for the Millon Song Dataset"
  :url "https://github.com/ryanmilvenan/ConcurrentlyKillingIt"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [jarohen/chord "0.4.2"]

                 [ring/ring-core "1.2.0"]
                 [compojure "1.2.0"]
                 [hiccup "1.0.4"]

                 [org.clojure/core.async "0.1.301.0-deb34a-alpha"]
                 [org.clojure/clojurescript "0.0-2268"]

                 [prismatic/dommy "0.1.2"]

                 [jarohen/clidget "0.2.0"]]

  :plugins [[lein-pdo "0.1.1"]
            [jarohen/lein-frodo "0.3.2"]
            [lein-cljsbuild "1.0.3"]
            [lein-shell "0.4.0"]]

  :frodo/config-resource "muzak-server-config.edn"

  :aliases {"start-server" ["do"
                   ["pdo"
                    ["cljsbuild" "auto"]
                    "frodo"]]}

  :source-paths ["src"]

  :resource-paths ["resources"]

  :cljsbuild {
    :builds [{
      :source-paths ["src-cljs"]
      :compiler {
        :output-to "resources/public/js/main.js"
        :optimizations :whitespace
        :pretty-print true}}]})
