package org.mint.utils

import org.scalatest.{AsyncWordSpec, Matchers}
import org.slf4j.MDC

import scala.concurrent.Future

class MDCPropagatingExecutionContextTest extends AsyncWordSpec with Matchers {
  implicit override val executionContext =
    MDCPropagatingExecutionContext(scala.concurrent.ExecutionContext.global)

  "MDCPropagatingExecutionContext" should {
    "propagate MDC values into a Future" in {
      MDC.put("requestId", "test-123")
      Future {
        MDC.get("requestId")
      }.map { value =>
        MDC.remove("requestId")
        value shouldEqual "test-123"
      }
    }

    "not leak MDC values after Future completes" in {
      MDC.put("requestId", "leak-test")
      val fut = Future { MDC.get("requestId") }
      MDC.remove("requestId")
      fut.map { _ =>
        MDC.get("requestId") shouldBe null
      }
    }
  }
}
