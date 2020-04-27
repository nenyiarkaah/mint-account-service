package org.mint.services

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.softwaremill.macwire.wire
import org.scalatest.{Matchers, WordSpec}
import org.mint.akkahttp.utils.TestData._
import org.mint.services.AccountService
import cats.instances.future.catsStdInstancesForFuture
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class AccountServiceTest extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {
  val service = wire[AccountService[Future]]

  "AccountService" should {
    "insert new account and return it's id" in {
      whenReady(service.insert(berlin)) {
        result =>
          val expectedId = 1
          result should equal(expectedId)
      }
    }
  }
}
