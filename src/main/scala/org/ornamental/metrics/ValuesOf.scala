package org.ornamental.metrics

import shapeless.::
import shapeless.HList
import shapeless.HNil

/**
 * Type family allowing to get a runtime `List[V]` from an literal `HList`, e.g. `List("a", "b",
 * "c")` from type `"a" shapeless.:: "b" shapeless.:: "c" shapeless.:: HNil`.
 *
 * @tparam H
 *   the `HList` type
 * @tparam V
 *   the value type (expected to have a `ValueOf` instance)
 */
trait ValuesOf[H <: HList, +V] {

  def apply(): List[V]
}

object ValuesOf {

  implicit def emptyValues[V]: ValuesOf[HNil, V] = () => Nil

  implicit def recurse[Q, V <: Q: ValueOf, T <: HList](
      implicit rest: ValuesOf[T, Q]
  ): ValuesOf[V :: T, Q] = () => implicitly[ValueOf[V]].value :: rest()
}
