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

  "insert" should {
    "insert new valid account and return it's id" in {
      val request = insertRequest(berlin)

      request ~> routes ~> check {
        commonChecks
        val count = entityAs[CommandResult].id
        val expectedId = 1
        count shouldEqual expectedId
      }
    }
    "reject a new account when account name is empty" in {
      val request = insertRequest(berlinWithEmptyName)

      request ~> routes ~> check {
        status shouldEqual StatusCodes.PreconditionFailed
      }
    }
    "reject a new account when account type is empty" in {
      val request = insertRequest(berlinWithEmptyAccountType)

      request ~> routes ~> check {
        status shouldEqual StatusCodes.PreconditionFailed
      }
    }
    "reject a new account when company is empty" in {
      val request = insertRequest(berlinWithEmptyCompany)

      request ~> routes ~> check {
        status shouldEqual StatusCodes.PreconditionFailed
      }
    }
  }


  "update" should {
    "update existing account with valid name and return it's id" in {
      val request = updateRequest(berlin, berlin.id)
      val expectedId = 1

      request ~> routes ~> check {
        commonChecks
        val id = entityAs[CommandResult].id
        id shouldEqual expectedId
      }
    }

  }

  private def commonChecks = {
    status shouldEqual StatusCodes.OK
    contentType shouldEqual ContentTypes.`application/json`
  }

  private def createStubRepo = {
    new Repository[Future] {
      override def insert(row: Account): Future[Int] = Future.successful(accountId)

      override def createSchema(): Future[Unit] = Future.successful(())

      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Account]] = ???

      override def sortingFields: Set[String] = ???

      override def selectAll: Future[Seq[Account]] = Future.successful(mockData)

      def select(id: Int): Future[Option[Account]] = ???

      override def update(id: Int, row: Account): Future[Int] = Future.successful(accountId)
    }
  }
}
