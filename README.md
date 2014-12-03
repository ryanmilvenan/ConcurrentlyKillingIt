ConcurrentlyKillingIt
=====================

An api and visualizer for the Million Song Database

Here are some resources that were used in constructing the HDF5 Parsing Code

cisd-JHDF5 binaries: https://wiki-bsse.ethz.ch/download/attachments/26609237/cisd-jhdf5-13.06.2-r29633.zip?version=1&modificationDate=1376120226957&api=v2

FOR Java Interop with Jars that lack a Maven repo, we found the Leiningen ":resource-paths" to work well
ref: http://stackoverflow.com/a/14070422/3314156
ex:
```
  :resource-paths ["resources"
                   "resources/lib/cisd-jhdf5-core.jar"
                   "resources/lib/cisd-jhdf5-tools.jar"
                   "resources/lib/cisd-jhdf5.jar"
                   "resources/lib/nativejar/hdf5-windows-intel.jar"]
```

but we did try other things with classpaths, ex:

    (System/getProperty "java.class.path")

Once you get the dependencies working, you need to learn how to use the "dot special form" for Java Interop from Closure
ref: http://clojure.org/java_interop


NOTE: The cisd-JHDF5 download above has some java code examples, "AttributeExample.java" and "FullVoltageMeasurementCompoundExample.java" were helpful.

Step 1 - call the HDFFactory openForReading method to get a IHDF5Reader

    (def hr (. ch.systemsx.cisd.hdf5.HDF5Factory openForReading "resources/TRAXLZU12903D05F94.h5"))
ref: http://svncisd.ethz.ch/doc/hdf5/hdf5-13.06/ch/systemsx/cisd/hdf5/HDF5Factory.html#openForReading(java.io.File)

Step 2 - Try reading a simple attribute with it

    (.getStringAttribute hr "metadata/artist_terms" "TITLE")


We quickly discovered that the HDF5 structures in the Million Song DataSet contain "Compound Data Sets"...

Some code examples show how to automatically deserialize HDF5 Compound Data sets into custom java classes, but without access to the Million Song DataBases java classes (and with hesitation to try to include more Java in Clojure), we looked for another way.

The "FullVoltageMeasurementCompoundExample.java" example showed the use of HDF5CompoundReader and HDF5CompoundDataMap

ref: ref: http://svncisd.ethz.ch/doc/hdf5/hdf5-13.06/ch/systemsx/cisd/hdf5/examples/FullVoltageMeasurementCompoundExample.jav
ref: http://svncisd.ethz.ch/doc/hdf5/hdf5-13.06/ch/systemsx/cisd/hdf5/HDF5CompoundDataMap.html

```
(def cr (.compound hr))
(def map (.read cr ("/metadata/songs" ch.systemsx.cisd.hdf5.HDF5CompoundDataMap)))
(get map "title")
```


