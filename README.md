# Prometheus Metrics Wrapper for `cats-effect`

Provides an interface (`Metrics[F[_]]`) and an implementation (`SyncMetrics[F[_]: Sync]`)
for collecting Prometheus metrics.

Compile-time labelling consistency is ensured by using custom case classes to represent label sets.

_Example_:

```
final case class QueryResultEvent(queryName: LabelValue, errored: LabelValue)
    extends LabelNames[("query_name", "has_errored")]

...
for {
    family <- Metrics[F].counter[QueryResultEvent](
        name = MetricName("query_result_counter"),
        help = "Counts finished query results")

    counter <- family.labelled(QueryResultEvent(
        calculation = LabelValue("select_recently_active_users"),
        errored = LabelValue("false")))
        
    _ <- counter.inc()
} yield ()
```

Each metrics family requested using `Metrics[F]` methods must have unique name and only be defined once (repeated
attempts for the same name will fail).\
Labelled metrics of a metrics family may be obtained using `labelled` any number of times (label set uniqueness is not
required).

To obtain the collected metrics in `application/openmetrics-text` format, use `Metrics[F].scrape` method.
