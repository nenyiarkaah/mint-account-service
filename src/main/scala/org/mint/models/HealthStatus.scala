package org.mint.models

case class HealthStatus(
  status: String,
  db: String,
  buildInfo: Map[String, String],
  uptimeMs: Long
)
