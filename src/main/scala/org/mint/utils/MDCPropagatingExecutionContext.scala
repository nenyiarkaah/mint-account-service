package org.mint.utils

import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class MDCPropagatingExecutionContext(delegate: ExecutionContext) extends ExecutionContextExecutor {
  override def execute(runnable: Runnable): Unit = {
    val mdcContext = MDC.getCopyOfContextMap
    delegate.execute(() => {
      val previous = MDC.getCopyOfContextMap
      if (mdcContext != null) { MDC.setContextMap(mdcContext) } else { MDC.clear() }
      try { runnable.run() }
      finally {
        if (previous != null) { MDC.setContextMap(previous) } else { MDC.clear() }
      }
    })
  }

  override def reportFailure(cause: Throwable): Unit = delegate.reportFailure(cause)
}

object MDCPropagatingExecutionContext {
  def apply(delegate: ExecutionContext): ExecutionContextExecutor =
    new MDCPropagatingExecutionContext(delegate)
}
