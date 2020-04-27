package org.mint.akkahttp

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.softwaremill.macwire.wire
import org.mint.akkahttp.utils.RequestSupport
import org.mint.models.CommandResult
import org.mint.services.AccountService
import org.scalatest.{Matchers, WordSpec}
import org.mint.akkahttp.utils.TestData._
import org.mint.json.SprayJsonFormat._
import cats.instances.future.catsStdInstancesForFuture

import scala.concurrent.Future

class CommandRoutesTest extends WordSpec with Matchers with ScalatestRouteTest {
  val service = wire[AccountService[Future]]
  val routes = wire[CommandRoutes].routes

  "CommandRoutes" should {
    "insert new account and return it's id" in {
      val request = RequestSupport.insertRequest(berlin)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        val count = entityAs[CommandResult].count
        count should ===(1)
      }
    }
  }
}
