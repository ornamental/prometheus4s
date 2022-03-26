package org.ornamental.metrics

import cats.data.NonEmptyList
import io.prometheus.client.CollectorRegistry

/**
 * Trait representing a metric context (a metric set exposed by the application or its part).
 * Implementations serve as a registry of metric families (see `MetricFamily`).
 *
 * @tparam F
 *   the effect type
 */
trait Metrics[F[_]] {

  import Metrics._

  /**
   * Creates a counter family having the specified name. The name must be unique.
   *
   * @param name
   *   the counter family name
   * @param help
   *   the comment to the counter family
   * @tparam L
   *   the label set type
   * @return
   *   the requested counter family, either newly created or existing
   */
  def counter[L: LabelSet](
      name: MetricName,
      help: String
  ): F[MetricFamily[F, CounterMetric, L]]

  /**
   * Creates a gauge family having the specified name. The name must be unique.
   *
   * @param name
   *   the gauge family name
   * @param help
   *   the comment to the gauge family
   * @tparam L
   *   the label set type
   * @return
   *   the requested gauge family, either newly created or existing
   */
  def gauge[L: LabelSet](name: MetricName, help: String): F[MetricFamily[F, GaugeMetric, L]]

  /**
   * Creates a histogram family having the specified name. The name must be unique.
   *
   * @param name
   *   the histogram family name
   * @param help
   *   the comment to the histogram family
   * @param buckets
   *   values of time series
   * @tparam L
   *   the label set type
   * @return
   *   the requested histogram family, either newly created or existing
   */
  def histogram[L: LabelSet](
      name: MetricName,
      help: String,
      buckets: NonEmptyList[Double] = DefaultHistogramBuckets
  ): F[MetricFamily[F, HistogramMetric, L]]

  /**
   * Creates a summary family having the specified name. The name must be unique.
   *
   * @param name
   *   the histogram family name
   * @param help
   *   the comment to the histogram family
   * @tparam L
   *   the label set type
   * @return
   *   the requested summary family, either newly created or existing
   */
  def summary[L: LabelSet](
      name: MetricName,
      help: String
  ): F[MetricFamily[F, SummaryMetric, L]]

  /**
   * Returns the current collected metrics of this metric set in `application/openmetrics-text`
   * format.
   *
   * @return
   *   the collected metrics' text representation
   */
  def scrape: F[String]

  /**
   * Returns the underlying collectors registry. The value is wrapped in effect suggesting that
   * its uses have side effects.
   *
   * @return
   *   the collector registry
   */
  def getRegistry: F[CollectorRegistry]
}

object Metrics {

  val DefaultHistogramBuckets: NonEmptyList[Double] =
    NonEmptyList(.005, List(.01, .025, .05, .075, .1, .25, .5, .75, 1, 2.5, 5, 7.5, 10))

  def apply[F[_]: Metrics]: Metrics[F] = implicitly
}
