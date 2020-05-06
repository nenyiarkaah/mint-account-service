package org.mint.akkahttp

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.mint.json.SprayJsonFormat._
import org.mint.models.{Account, CommandResult}
import org.mint.repositories.Repository
import org.mint.services.AccountService
import org.mint.utils.RequestSupport._
import org.mint.utils.TestData._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class CommandRoutesTest extends WordSpec with Matchers with ScalatestRouteTest {
  val service = wire[AccountService[Future]]
  val routes = wire[CommandRoutes].routes

  "CommandRoutes" should {
    "insert new account and return it's id" in {
      val request = insertRequest(berlin)

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

      override def createSchema(): Future[Unit] = Future.successful(())

      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Account]] = Future.successful(mockData)

      override def sortingFields: Set[String] = Set("id", "name")
    }
  }
}
