package org.ornamental.metrics

/**
 * Trait representing a metric family. Metrics in a metric family have the same type (counter,
 * gauge, &c.) and same label names, but differ in label values.
 *
 * @tparam F
 *   the effect type
 * @tparam M
 *   the metric type of the metric family
 * @tparam L
 *   the type representing the label sets of the metric family
 */
trait MetricFamily[F[_], M[_[_]], L] {

  /**
   * Returns a single metric of the metric family for a particular label set.
   *
   * @param labels
   *   the label set to return the metric for
   * @return
   *   the metric having the requested label, either newly registered or already existing
   */
  def labelled(labels: L): F[M[F]]
}
