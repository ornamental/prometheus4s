package org.ornamental

import cats.kernel.Eq
import shapeless.{::, HNil}

import scala.util.matching.Regex
import supertagged.{TaggedOps, TaggedType0}

package object metrics {

  object MetricName extends TaggedType0[String] {

    implicit val equality: Eq[MetricName] = Eq.fromUniversalEquals

    val regex: Regex = "[a-zA-Z_:][a-zA-Z0-9_:]*".r

    def apply(s: String): MetricName =
      if (regex.matches(s)) TaggedOps(this)(s)
      else
        throw new AssertionError("Metric name must match the regex /[a-zA-Z_:][a-zA-Z0-9_:]*/.")
  }
  type MetricName = MetricName.Type

  object LabelName extends TaggedType0[String] {

    implicit val equality: Eq[LabelName] = Eq.fromUniversalEquals

    val regex: Regex = "[a-zA-Z][a-zA-Z0-9_]*".r

    def apply(s: String): LabelName =
      if (regex.matches(s)) TaggedOps(this)(s)
      else throw new AssertionError("Label name must match the regex /[a-zA-Z][a-zA-Z0-9_]*/.")
  }
  type LabelName = LabelName.Type

  object LabelValue extends TaggedType0[String] {

    implicit val equality: Eq[LabelValue] = Eq.fromUniversalEquals

    def apply(s: String): LabelValue =
      if (s.nonEmpty) TaggedOps(this)(s)
      else throw new AssertionError("Label value must not be empty.")
  }
  type LabelValue = LabelValue.Type

  object NonNegativeAmount extends TaggedType0[Double] {

    def apply(v: Double): NonNegativeAmount =
      if (v >= 0) TaggedOps(this)(v)
      else throw new AssertionError("Amount must be non-negative.")
  }
  type NonNegativeAmount = NonNegativeAmount.Type

  type |:[+P, +L] = P :: L :: HNil
}
