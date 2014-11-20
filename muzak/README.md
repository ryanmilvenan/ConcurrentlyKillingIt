# muzak



## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Installation

Three .jar files are not included must be manually "installed" to run the program. In your "resources" directory, make a folder called lib and add the .jar files to it. 

## Running

To start a web server for the application, run:

    lein start-server

When the server has finished initializing, point your web browser to "localhost:3000" to view the web page.

## HDF5 Parsing
After launching a repl, load the handler.clj with
`(load-file "src/muzak/core/handler.clj")`
and switch namespace
`(ns muzak.core.handler)`
then open the in-repository "resources\...h5"
'(def hr (hdf5-get-reader))`
confirm that you've got a HDF5Reader object
`(type hr)`


## License

Copyright © 2014 FIXME
