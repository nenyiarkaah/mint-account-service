package org.mint.akkahttp

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.mint.akkahttp.utils.RequestSupport
import org.mint.akkahttp.CommandRoutes
import org.mint.models.CommandResult
import org.mint.services.AccountService
import org.scalatest.{Matchers, WordSpec}
import org.mint.akkahttp.utils.TestData._
import org.mint.json.SprayJsonFormat._
import cats.instances.future.catsStdInstancesForFuture
import org.mint.repositories.Repository
import org.mint.models.Account

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
        val expectedId = 1
        count should ===(expectedId)
      }
    }
  }

  private def createStubRepo = {
    new Repository[Future] {
      override def insert(row: Account): Future[Int] = Future.successful(accountId)
    }
  }
}
