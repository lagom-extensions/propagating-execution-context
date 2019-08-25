package com.lightbend.lagom.concurrent

import org.slf4j.MDC

case class MDCThreadStatePropagator() extends ThreadStatePropagator[java.util.Map[String, String]] {
  override def threadState: java.util.Map[String, String] = MDC.getCopyOfContextMap
  override def wrapWithState(runnable: Runnable, state: java.util.Map[String, String]): Runnable = () => {
    val previous = MDC.getCopyOfContextMap
    setMDC(state)
    try {
      runnable.run()
    } finally {
      setMDC(previous)
    }
  }
  private def setMDC(contextMap: java.util.Map[String, String]): Unit = {
    if (contextMap == null) MDC.clear()
    else MDC.setContextMap(contextMap)
  }
}
