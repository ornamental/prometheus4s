package org.ornamental.metrics

import shapeless.Generic
import shapeless.HList
import shapeless.Nat
import shapeless.ops.hlist.Length
import shapeless.ops.hlist.ToList

/**
 * To use with final case classes describing label sets.<br/> Extend this trait with type
 * parameter being a chain of label names to infer a `LabelSet` instance for your case
 * class.<br/> <em>Example.</br>
 * {{{
 *   final case class MyLabels(service: LabelValue, operation: LabelValue)
 *     extends LabelNames[("service_name", "operation_code")]
 * }}}
 * In order for the inference to succeed, the number of fields must match the number of label
 * names, the label names must all be string literals, and the fields must all have type
 * `LabelValue`.
 *
 * @tparam L
 *   the chain of label names represented as any type convertible to `HList` of literal types
 *   (e.g. HList itself or a tuple)
 */
trait LabelNames[L]

object LabelNames {

  implicit def labelSet[L, G, H <: HList, F <: HList, LH <: Nat, LF <: Nat](
      implicit representable: Generic.Aux[L, F],
      isLabelNames: L <:< LabelNames[G],
      asHlist: Generic.Aux[G, H],
      lengthH: Length.Aux[H, LH],
      lengthF: Length.Aux[F, LF],
      lengthsMatch: LH =:= LF,
      toList: ToList[F, LabelValue],
      labelNames: ValuesOf[H, String]
  ): LabelSet[L] = new LabelSet[L] {

    private val labelNamesMapped: List[LabelName] = labelNames().map(LabelName(_))

    override def names: List[LabelName] = labelNamesMapped

    override def values(labels: L): List[LabelValue] = toList(representable.to(labels))
  }
}
