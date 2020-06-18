package org.mint.akkahttp

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.{Route}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.mint.json.SprayJsonFormat._
import org.mint.models.{Account, AccountTypes, Accounts}
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
        commonChecks
        val response = entityAs[Accounts].accounts
          response.length shouldEqual 3
          response should contain(geneva)
          response should contain(paris)
          response should contain(madrid)
      }
    }
    "return a list of accounts sorted by id" in {
      val page = Some(1)
      val pageSize = Some(1)
      val sort = "id"
      val request = selectAllRequest(sort)
      val expectedAccounts = Seq(geneva, paris, madrid)

      request ~> routes ~> check {
        commonChecks
        val response = entityAs[Accounts].accounts
        response.length shouldEqual 3
        response shouldEqual expectedAccounts
      }
    }
    "return a list of accounts sorted by account name" in {
      val page = Some(1)
      val pageSize = Some(1)
      val sort = "name"
      val request = selectAllRequest(sort)
      val expectedAccounts = Seq(geneva, madrid, paris)

      request ~> routes ~> check {
        commonChecks
        val response = entityAs[Accounts].accounts
        response.length shouldEqual 3
        response shouldEqual expectedAccounts
      }
    }
  }


  "select account by id" should {
    "return an account when the id is valid" in {
      val id: Int = 4

      val request = selectByRequest(id)
      request ~> routes ~> check {
        commonChecks
        val response = entityAs[Option[Account]]
        response shouldEqual Some(madrid)
      }
    }
    "return empty when the id is invalid" in {
      val id = 2222
      val select = selectByRequest(id)
      select ~> Route.seal(routes) ~> check {
        failedRequestCheck
      }
    }
  }

  "existingTypeofAccounts" should {
    "return a type of account list of 2 for mockData" in {
      val request = existingTypeofAccountsRequest
      request ~> routes ~> check {
        commonChecks
        val response = entityAs[AccountTypes].accountTypes
        response.length shouldEqual 2
        response contains "test"
        response contains "current"
      }
    }
  }


  private def commonChecks = {
    val expectedStatusCode = StatusCodes.OK
    val contentType = ContentTypes.`application/json`
    if (expectedStatusCode !== status) println(s"*** Response body: $responseEntity")
    status shouldEqual expectedStatusCode
    contentType shouldEqual contentType
  }

  private def failedRequestCheck = {
    val expectedStatusCode = StatusCodes.NotFound
    status shouldEqual expectedStatusCode
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

      def select(id: Int): Future[Option[Account]] =
        Future.successful(mockData.filter(_.id === id).headOption)

      override def update(id: Int, row: Account): Future[Int] = ???

      override def delete(id: Int): Future[Int] = ???
    }
  }
}
