
# About Spark

### RDDs (Resilient Distributed Datasets)
The data backbone that allows clustered processing to be so performant.
Basically any data that you are working with.

### Closures
One of the more challenging aspects of Spark is understanding how
closures work - and therefore writing code that does what you expect it
to do.

The example spark provides:

```
var counter = 0
var rdd = sc.parallelize(data)

// Wrong: Don't do this!!
rdd.foreach(x => counter += x)

println("Counter value: " + counter)
```

`The closure is those variables and methods which must be visible for the
 executor to perform its computations on the RDD`
 =
 All the code necessary for the data processing

- the closure is serialized and sent to each executor (remember clustered)
- final value of counter will still be zero

In general, closures - constructs like loops or locally defined methods,
should not be used to mutate some global state.


#### Solution: `Accumulator` *or*  `.collect` *or* `Broadcast`
- Accumulator
    - by using an Accumulator you instruct Spark to handle variables appropriately
    ```
    val accum = sc.accumulator(0, "My Accumulator")
    sc.parallelize(Array(1, 2, 3, 4)).foreach(x => accum += x)
    println(accum.value) // prints 10
    ```

- .collect()
    - use the collect() method to first bring the RDD to the driver node:
    `rdd.collect().foreach(println)`

- Broadcast variables
    - allow the programmer to keep a read-only variable cached on each
    machine rather than shipping a copy of it with tasks
    - spark automatically broadcasts the common data needed by tasks
    - explicitly creating broadcast variables is only useful when:
        - tasks across multiple stages need the same data or
        - when caching the data in deserialized form is important

    ```
    val broadcastVar = sc.broadcast(Array(1, 2, 3))
    println(broadcastVar.value) // prints Array(1, 2, 3)
    ```

