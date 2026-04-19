package org.mint.metrics

import com.codahale.metrics.{MetricRegistry, MetricSet}
import com.codahale.metrics.SharedMetricRegistries
import com.codahale.metrics.jvm.{GarbageCollectorMetricSet, JvmAttributeGaugeSet, MemoryUsageGaugeSet, ThreadStatesGaugeSet}

import scala.collection.JavaConverters._

class MetricsRegistry {
  val registry: MetricRegistry = SharedMetricRegistries.getOrCreate("mint-account")
  registerIfAbsent(new JvmAttributeGaugeSet())
  registerIfAbsent(new GarbageCollectorMetricSet())
  registerIfAbsent(new MemoryUsageGaugeSet())
  registerIfAbsent(new ThreadStatesGaugeSet())

  private def registerIfAbsent(set: MetricSet): Unit = {
    val existing = registry.getNames
    set.getMetrics.asScala.foreach {
      case (name, metric) =>
        if (!existing.contains(name)) { registry.register(name, metric) }
    }
  }
}
