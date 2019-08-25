package com.lightbend.lagom.concurrent

import java.util.concurrent.Executors

import org.scalatest.{Assertion, AsyncWordSpec, Matchers}
import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, Future}

class PropagatingExecutionContextTest extends AsyncWordSpec with Matchers {
  "PropagatingExecutionContext" should {
    "propagate MDC check for comprehension" in {
      implicit val executionContext: ExecutionContext = mdcExecutionContext
      MDC.put(testPropagatedMDCKey, testPropagatedMDCValue)
      for {
        _ <- check()
        _ <- check()
        _ <- check()
      } yield succeed
    }

    "propagate MDC check work via multiple execution contexts" in {
      implicit val executionContext: ExecutionContext = mdcExecutionContext
      val ec1: ExecutionContext = mdcExecutionContext
      val ec2: ExecutionContext = mdcExecutionContext
      MDC.put(testPropagatedMDCKey, testPropagatedMDCValue)
      for {
        _ <- check()(ec1)
        _ <- check()(ec2)
        _ <- check()(ec1)
      } yield succeed
    }
  }

  val testPropagatedMDCKey = "test_propagated_key"
  val testPropagatedMDCValue = "test_propagated_value"

  def check()(implicit ec: ExecutionContext = mdcExecutionContext): Future[Assertion] = {
    Future {
      MDC.getCopyOfContextMap.get(testPropagatedMDCKey) shouldBe testPropagatedMDCValue
    }(ec)
  }

  private def mdcExecutionContext: ExecutionContext =
    PropagatingExecutionContext.mdcPropagating(ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor()))
}
