package org.ornamental.metrics

import cats.effect.Resource

/**
 * Summary metric to track the size of events
 *
 * @tparam F
 *   \- the effect type
 */
trait SummaryMetric[F[_]] {

  /**
   * Start a timer to track a duration and returns a token to stop the timer
   * @return
   *   started timer token
   */
  def startObservation: F[F[Unit]]

  /**
   * Observe the amount of time in seconds since startTimer was called, returning the resource
   * to use.
   * @return
   *   resource to use on the effect and observe the duration as a finalizer
   */
  def observeDuration: Resource[F, Unit]

  /**
   * Add value to the summary metrics
   */
  def observe(value: Double): F[Unit]

}
