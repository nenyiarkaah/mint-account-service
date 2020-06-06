package org.mint.akkahttp

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.mint.json.SprayJsonFormat._
import org.mint.models.{Account, Accounts}
import org.mint.repositories.Repository
import org.mint.services.AccountService
import org.mint.utils.RequestSupport._
import org.mint.utils.TestData._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class QueryRoutesTest extends WordSpec with Matchers with ScalatestRouteTest {
  val service = wire[AccountService[Future]]
  val routes = wire[QueryRoutes].routes

  "selectAll" should {
    "return a list of accounts" in {
      val page = Some(1)
      val pageSize = Some(1)
      val sort = Some("id")
      val request = selectAllRequest

      request ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val results = entityAs[Accounts].accounts
          results.length shouldEqual 3
        results should contain(geneva)
          results should contain(paris)
          results should contain(madrid)
      }
    }
    "return a list of accounts sorted by id" in {
      val page = Some(1)
      val pageSize = Some(1)
      val sort = "id"
      val request = selectAllRequest(sort)
      val expectedAccounts = Seq(geneva, paris, madrid)

      request ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val results = entityAs[Accounts].accounts
        results.length shouldEqual 3
        results shouldEqual expectedAccounts
      }
    }
    "return a list of accounts sorted by account name" in {
      val page = Some(1)
      val pageSize = Some(1)
      val sort = "name"
      val request = selectAllRequest(sort)
      val expectedAccounts = Seq(geneva, madrid, paris)

      request ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val results = entityAs[Accounts].accounts
        results.length shouldEqual 3
        results shouldEqual expectedAccounts
      }
    }
  }

  "existingTypeofAccounts" should {
    "return a type of account list of 2 for mockData" in {
      val request = existingTypeofAccountsRequest
      request ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
      }
    }
  }

  private def createStubRepo = {
    new Repository[Future] {
      override def insert(row: Account): Future[Int] = ???

      override def createSchema(): Future[Unit] = ???

      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Account]] = {
        sort match {
          case "id" => Future.successful (mockData.sortBy(_.id))
          case "name" => Future.successful (mockData.sortBy(_.name))
        }
      }

      override def sortingFields: Set[String] = Set("id", "name")

      override def selectAll: Future[Seq[Account]] = Future.successful (mockData)
    }
  }
}
