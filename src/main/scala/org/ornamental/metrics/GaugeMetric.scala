package org.ornamental.metrics

/**
 * Represents a gauge, i.e. a time series whose value starts from 0 and may both increase and
 * decrease. Both positive and negative values are allowed.
 *
 * @tparam F
 *   the effect type
 */
trait GaugeMetric[F[_]] {

  /**
   * Increments the gauge value by 1.
   *
   * @return
   *   the completion token
   */
  def inc(): F[Unit] = adjustBy(1.0)

  /**
   * Decrements the gauge value by 1.
   *
   * @return
   *   the completion token
   */
  def dec(): F[Unit] = adjustBy(-1.0)

  /**
   * Changes the gauge value by the desired amount, positive or not.
   *
   * @param amount
   *   the amount to change the gauge value by
   * @return
   *   the completion token
   */
  def adjustBy(amount: Double): F[Unit]

  /**
   * Sets the gauge value.
   *
   * @param value
   *   the value to set
   * @return
   *   the completion token
   */
  def set(value: Double): F[Unit]
}
