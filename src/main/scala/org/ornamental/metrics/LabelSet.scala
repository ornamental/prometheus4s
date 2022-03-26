package org.ornamental.metrics

/**
 * Type class for types representing metric label sets.<br/> Instances of the type class may be
 * used to label time series.
 *
 * @tparam L
 *   the type representing a label set
 */
trait LabelSet[L] {

  /**
   * Returns label names. This must return the same `List` on all invocations.
   *
   * @return
   *   the list of label names
   */
  def names: List[LabelName]

  /**
   * Extracts label values from an instance of the underlying type.<br/> The length of the
   * resulting list must match that of `names`. The values must be ordered so that zipping
   * `names` with the result produces a valid list of (label name, label value) pairs.
   *
   * @param labels
   *   the instance of the underlying type providing the label values
   * @return
   *   the list of label values corresponding to the list returned by `names`
   */
  def values(labels: L): List[LabelValue]
}

object LabelSet {

  /**
   * Empty label set. Corresponds to labelling with `()`.
   */
  implicit val empty: LabelSet[Unit] = new LabelSet[Unit] {

    override def names: List[LabelName] = Nil

    override def values(labels: Unit): List[LabelValue] = Nil
  }
}
