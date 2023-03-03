package org.mint.akkahttp

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.mint.json.SprayJsonFormat._
import org.mint.models.{Account, CommandResult}
import org.mint.repositories.{AccountRepository, Repository}
import org.mint.services.AccountService
import org.mint.unit.utils.RequestSupport._
import org.mint.utils.TestData._
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.mockito.MockitoSugar.mock
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class CommandRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with MockitoSugar with ScalaFutures  {
  val repository = mock[AccountRepository]
  val service = wire[AccountService]
  val routes = wire[CommandRoutes].routes

  "insert" should {
    "insert new valid account and return it's id" in {
      val request = insertRequest(berlin)
      val expectedId = 1
      when(repository.selectAll) thenReturn Future.successful(accounts)
      when(repository.insert(berlin)) thenReturn Future.successful(expectedId)
      request ~> routes ~> check {
        commonChecks
        val count = entityAs[CommandResult].id
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
      val expectedId = berlin.id
      when(repository.update( berlin.id, berlin)) thenReturn Future.successful(expectedId)

      request ~> routes ~> check {
        commonChecks
        val id = entityAs[CommandResult].id
        id shouldEqual expectedId
      }
    }

    "delete" should {
      "delete an account based on id" in {
        val id = berlin.id
        val request = deleteRequest(id)
        when(repository.delete(id)) thenReturn Future.successful(id)

        request ~> routes ~> check {
          commonChecks
          val response = entityAs[CommandResult]
          response.id shouldEqual id
        }
      }
    }
  }

  private def commonChecks = {
    status shouldEqual StatusCodes.OK
    contentType shouldEqual ContentTypes.`application/json`
  }
}
