package org.ornamental.metrics

import java.io.StringWriter
import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.Resource
import cats.effect.kernel.Sync
import cats.syntax.applicative._
import cats.syntax.functor._
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.Histogram
import io.prometheus.client.SimpleCollector
import io.prometheus.client.Summary
import io.prometheus.client.exporter.common.TextFormat

final class SyncMetrics[F[_]: Sync] private (registry: CollectorRegistry) extends Metrics[F] {

  override def counter[L: LabelSet](
      name: MetricName,
      help: String
  ): F[MetricFamily[F, CounterMetric, L]] =
    buildCounter(name, help).map(SyncMetrics.CounterFamilyImpl[F, L](_))

  override def gauge[L: LabelSet](
      name: MetricName,
      help: String
  ): F[MetricFamily[F, GaugeMetric, L]] =
    buildGauge(name, help).map(SyncMetrics.GaugeFamilyImpl[F, L](_))

  override def histogram[L: LabelSet](
      name: MetricName,
      help: String,
      buckets: NonEmptyList[Double] = Metrics.DefaultHistogramBuckets
  ): F[MetricFamily[F, HistogramMetric, L]] =
    buildHistogram(name, help, buckets).map(SyncMetrics.HistogramFamilyImpl[F, L](_))

  override def summary[L: LabelSet](
      name: MetricName,
      help: String
  ): F[MetricFamily[F, SummaryMetric, L]] =
    buildSummary(name, help).map(SyncMetrics.SummaryFamilyImpl[F, L](_))

  override def scrape: F[String] = Sync[F].delay {
    val output = new StringWriter()
    TextFormat.writeFormat(
      TextFormat.CONTENT_TYPE_OPENMETRICS_100,
      output,
      registry.metricFamilySamples()
    )
    output.toString
  }

  override def getRegistry: F[CollectorRegistry] = registry.pure[F]

  private def buildCounter[L: LabelSet](name: MetricName, help: String): F[Counter] =
    Sync[F].delay {
      Counter.build(name, help).labelNames(implicitly[LabelSet[L]].names: _*).register(registry)
    }

  private def buildGauge[L: LabelSet](name: MetricName, help: String): F[Gauge] =
    Sync[F].delay {
      Gauge.build(name, help).labelNames(implicitly[LabelSet[L]].names: _*).register(registry)
    }

  private def buildHistogram[L: LabelSet](
      name: MetricName,
      help: String,
      buckets: NonEmptyList[Double]
  ): F[Histogram] =
    Sync[F].delay {
      Histogram
        .build(name, help)
        .buckets(buckets.toList: _*)
        .labelNames(implicitly[LabelSet[L]].names: _*)
        .register(registry)
    }

  private def buildSummary[L: LabelSet](name: MetricName, help: String): F[Summary] =
    Sync[F].delay {
      Summary.build(name, help).labelNames(implicitly[LabelSet[L]].names: _*).register(registry)
    }
}

object SyncMetrics {

  def apply[F[_]: Async]: F[SyncMetrics[F]] =
    Sync[F].delay(new CollectorRegistry()).map(new SyncMetrics[F](_))

  final private case class CounterFamilyImpl[F[_]: Sync, L: LabelSet](counter: Counter)
      extends MetricFamily[F, CounterMetric, L] {

    override def labelled(labels: L): F[CounterMetric[F]] =
      getChild(counter, labels).map(CounterMetricImpl(_))
  }

  final private case class CounterMetricImpl[F[_]: Sync](singleCounter: Counter.Child)
      extends CounterMetric[F] {

    override def inc(amount: NonNegativeAmount): F[Unit] =
      Sync[F].delay { singleCounter.inc(amount) }
  }

  final private case class GaugeFamilyImpl[F[_]: Sync, L: LabelSet](gauge: Gauge)
      extends MetricFamily[F, GaugeMetric, L] {

    override def labelled(labels: L): F[GaugeMetric[F]] =
      getChild(gauge, labels).map(GaugeMetricImpl(_))
  }

  final private case class GaugeMetricImpl[F[_]: Sync](singleGauge: Gauge.Child)
      extends GaugeMetric[F] {

    override def adjustBy(amount: Double): F[Unit] =
      Sync[F].delay { if (amount >= 0) singleGauge.inc(amount) else singleGauge.dec(-amount) }

    override def set(value: Double): F[Unit] =
      Sync[F].delay { singleGauge.set(value) }
  }

  final private case class HistogramFamilyImpl[F[_]: Sync, L: LabelSet](histogram: Histogram)
      extends MetricFamily[F, HistogramMetric, L] {

    override def labelled(labels: L): F[HistogramMetric[F]] =
      getChild(histogram, labels).map(HistogramMetricImpl(_))
  }

  final private case class HistogramMetricImpl[F[_]: Sync](singleHistogram: Histogram.Child)
      extends HistogramMetric[F] {

    override def startObservation: F[F[Unit]] =
      startObservationF(() => singleHistogram.startTimer())(_.observeDuration())

    override def observeDuration: Resource[F, Unit] =
      observeDurationF(() => singleHistogram.startTimer())(_.observeDuration())

    override def observe(value: Double): F[Unit] = Sync[F].delay(singleHistogram.observe(value))
  }

  final private case class SummaryFamilyImpl[F[_]: Sync, L: LabelSet](summary: Summary)
      extends MetricFamily[F, SummaryMetric, L] {

    override def labelled(labels: L): F[SummaryMetric[F]] =
      getChild(summary, labels).map(SummaryMetricImpl(_))
  }

  final private case class SummaryMetricImpl[F[_]: Sync](singleSummary: Summary.Child)
      extends SummaryMetric[F] {

    override def startObservation: F[F[Unit]] =
      startObservationF(() => singleSummary.startTimer())(_.observeDuration())

    override def observeDuration: Resource[F, Unit] =
      observeDurationF(() => singleSummary.startTimer())(_.observeDuration())

    override def observe(value: Double): F[Unit] = Sync[F].delay(singleSummary.observe(value))
  }

  private def getChild[F[_]: Sync, A, B, L: LabelSet](sc: SimpleCollector[A], labels: L): F[A] =
    Sync[F].delay { sc.labels(implicitly[LabelSet[L]].values(labels): _*) }

  private def startObservationF[F[_]: Sync, A](
      startTimerUnsafe: () => A
  )(observeDurationUnsafe: A => Double): F[F[Unit]] =
    Sync[F]
      .delay(startTimerUnsafe())
      .map(timer => Sync[F].delay(observeDurationUnsafe(timer)).void)

  private def observeDurationF[F[_]: Sync, A](
      startTimerUnsafe: () => A
  )(observeDurationUnsafe: A => Double): Resource[F, Unit] =
    Resource
      .make(Sync[F].delay(startTimerUnsafe()))(timer =>
        Sync[F].delay(observeDurationUnsafe(timer)))
      .as(())
}
