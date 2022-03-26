package org.ornamental.metrics

/**
 * Represents a counter, i.e. a time series whose value starts from zero and does not decrease.
 * Counter value is reset to 0 as soon as the counter is recreated (e.g. upon application
 * restart).
 *
 * @tparam F
 *   the effect type
 */
trait CounterMetric[F[_]] {

  /**
   * Increments the counter by 1.
   *
   * @return
   *   the completion token
   */
  def inc(): F[Unit] = inc(NonNegativeAmount(1.0))

  /**
   * Increments the counter by the desired amount.
   *
   * @param amount
   *   the amount to increment the counter by
   * @return
   *   the completion token
   */
  def inc(amount: NonNegativeAmount): F[Unit]
}
