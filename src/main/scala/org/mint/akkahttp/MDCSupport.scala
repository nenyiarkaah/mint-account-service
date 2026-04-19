package org.mint.akkahttp

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.slf4j.MDC

import java.util.UUID

object MDCSupport {
  def withRequestMDC(inner: => Route): Route = extractRequest { req =>
    val requestId = UUID.randomUUID().toString
    MDC.put("requestId", requestId)
    MDC.put("httpMethod", req.method.value)
    MDC.put("path", req.uri.path.toString)
    val result = inner
    MDC.remove("requestId")
    MDC.remove("httpMethod")
    MDC.remove("path")
    result
  }
}
