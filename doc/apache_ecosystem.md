HADOOP
===========================

- Not a DB
- clustered storage and processing
- Use MapReduce to consume data
- default file system is HDFS
- can use other FileSystems e.g. S3, CassandraFS

### Limitations
- one job at a time
- batch processing
- FIFO jobs (**Cascading** Framework solves this?)
- Job output needs to be stored in FS before next step can start

### Components
- JobTracker: divides jobs to TaskTrackers

### Tech notes
- reportedly badly written Java
- MapReduce component superseded by "Spark + FS" (FS = distributed file system)

### Conclusions
- Often only used for Data Warehousing


HIVE
===========================

- for Hadoop
- converts Query language (HiveQL) to MapReduce jobs (because easier than writing MapReduce)


SPARK
===========================
Framework built around speed, ease-of-use, and sophisticated analytics.

- works with diverse data sets, batch or streaming
- write in Java, Scala, Python, Clojure, R
- map, reduce, SQL, ML, graph data processing
- directed acyclic graph (DAG) pattern: allows complex multi-step pipelines
- in-memory data sharing across DAGs
- lazy evaluation of queries
- intermediate results in memory (spills to disk)
- interactive shell

### Components
- Spark Streaming
- Spark SQL
- Spark MLlib
- Spark GraphX
    - GraphX extends the Spark RDD by introducing the Resilient Distributed Property Graph
    - a set of fundamental operators (e.g., subgraph, joinVertices, and aggregateMessages) as well as an optimized variant of the Pregel API


### Tech notes
- written in Scala


===========================