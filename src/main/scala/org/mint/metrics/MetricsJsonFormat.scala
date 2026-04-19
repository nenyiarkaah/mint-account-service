package org.mint.metrics

import com.codahale.metrics.{Counter, Gauge, MetricRegistry, Timer}
import spray.json.{JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

import java.util.concurrent.TimeUnit
import scala.collection.JavaConverters._

object MetricsJsonFormat {
  implicit val metricsRegistryFormat: RootJsonFormat[MetricRegistry] =
    new RootJsonFormat[MetricRegistry] {
      def write(reg: MetricRegistry): JsValue = JsObject(
        "counters" -> countersJson(reg),
        "timers" -> timersJson(reg),
        "gauges" -> gaugesJson(reg)
      )
      def read(json: JsValue): MetricRegistry =
        throw new UnsupportedOperationException("MetricRegistry is write-only")
    }

  private def countersJson(reg: MetricRegistry): JsValue = {
    val fields = reg.getCounters.asScala.map {
      case (name, c: Counter) => name -> JsObject("count" -> JsNumber(c.getCount))
    }.toMap
    JsObject(fields)
  }

  private def timersJson(reg: MetricRegistry): JsValue = {
    val fields = reg.getTimers.asScala.map {
      case (name, t: Timer) =>
        val snap = t.getSnapshot
        name -> JsObject(
          "count" -> JsNumber(t.getCount),
          "meanMs" -> JsNumber(round(snap.getMean / 1e6)),
          "p99Ms" -> JsNumber(round(snap.get99thPercentile() / 1e6)),
          "p999Ms" -> JsNumber(round(snap.get999thPercentile() / 1e6)),
          "meanRate" -> JsNumber(round(t.getMeanRate))
        )
    }.toMap
    JsObject(fields)
  }

  private def gaugesJson(reg: MetricRegistry): JsValue = {
    val fields = reg.getGauges.asScala.flatMap {
      case (name, g: Gauge[_]) =>
        safeGaugeValue(g.getValue).map(v => name -> JsObject("value" -> v))
    }.toMap
    JsObject(fields)
  }

  private def safeGaugeValue(v: Any): Option[JsValue] = v match {
    case n: Number => Some(JsNumber(n.doubleValue()))
    case s: String => Some(JsString(s))
    case _ => None
  }

  private def round(d: Double): BigDecimal =
    BigDecimal(d).setScale(3, BigDecimal.RoundingMode.HALF_UP)
}
