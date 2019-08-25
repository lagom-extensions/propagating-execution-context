package com.lightbend.lagom.concurrent

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object PropagatingExecutionContext {
  def mdcPropagating(delegate: ExecutionContext) =
    PropagatingExecutionContext(delegate: ExecutionContext, Seq(MDCThreadStatePropagator()))
}

case class PropagatingExecutionContext(delegate: ExecutionContext, propagators: Seq[ThreadStatePropagator[_]])
    extends ExecutionContextExecutor { self =>
  override def execute(command: Runnable): Unit = delegate.execute(command)
  override def reportFailure(cause: Throwable): Unit = delegate.reportFailure(cause)

  override def prepare(): ExecutionContext = new ExecutionContext {
    private val callerSideState: Seq[CapturedThreadState[_]] = propagators.map(p => p.capture)
    override def execute(runnable: Runnable): Unit =
      self.execute(callerSideState.foldLeft(runnable)((acc, curr) => curr.wrap(acc)))
    override def reportFailure(cause: Throwable): Unit = self.reportFailure(cause)
    override def prepare(): ExecutionContext = self.prepare()
  }
}

case class CapturedThreadState[S](propagator: ThreadStatePropagator[S], state: S) {
  def wrap(runnable: Runnable): Runnable = propagator.wrapWithState(runnable, state)
}
trait ThreadStatePropagator[S] {
  private[concurrent] def capture: CapturedThreadState[S] = CapturedThreadState[S](this, threadState)
  def threadState: S
  def wrapWithState(runnable: Runnable, state: S): Runnable
}
