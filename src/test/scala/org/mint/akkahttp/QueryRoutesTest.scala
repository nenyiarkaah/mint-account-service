package org.mint.akkahttp

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.mint.json.SprayJsonFormat._
import org.mint.models.{Account, AccountTypes, HealthStatus, ImportStatus}
import org.mint.metrics.MetricsRegistry
import org.mint.repositories.{AccountRepository, Repository}
import org.mint.services.AccountService
import org.mint.unit.utils.RequestSupport.{metricsRequest, _}
import org.mint.utils.TestData._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.mockito.MockitoSugar.mock
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class QueryRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with MockitoSugar with ScalaFutures  {
  val repository = mock[AccountRepository]
  val metricsRegistry = new MetricsRegistry
  val service = wire[AccountService]
  val routes = wire[QueryRoutes].routes

  "selectAll" should {
    "return a list of accounts" in {
      val request = selectAllRequest
      when(repository.selectAll(any[Int], any[Int], any[String])) thenReturn Future.successful(accounts)

      request ~> routes ~> check {
        commonChecks
        val response = entityAs[Seq[Account]]
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
      when(repository.selectAll(any[Int], any[Int], any[String])) thenReturn Future.successful(accounts)
      when(repository.sortingFields) thenReturn Set("id")
      request ~> routes ~> check {
        commonChecks
        val response = entityAs[Seq[Account]]
        response.length shouldEqual 3
        response shouldEqual expectedAccounts
      }
    }
    "return 400 when sort field is unknown" in {
      val request = selectAllRequest("bogus")
      when(repository.sortingFields) thenReturn Set("id", "name")
      request ~> Route.seal(routes) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
    "return 400 when page is negative" in {
      val request = selectAllRequest(-1, 10)
      when(repository.sortingFields) thenReturn Set("id", "name")
      request ~> Route.seal(routes) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
    "return 400 when pageSize is zero" in {
      val request = selectAllRequest(0, 0)
      when(repository.sortingFields) thenReturn Set("id", "name")
      request ~> Route.seal(routes) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
  }

  "select account by id" should {
    "return an account when the id is valid" in {
      val id: Int = 4
      val request = selectByRequest(id)
      when(repository.select(id)) thenReturn Future.successful(Some(madrid))

      request ~> routes ~> check {
        commonChecks
        val response = entityAs[Option[Account]]
        response shouldEqual Some(madrid)
      }
    }
    "return empty when the id is invalid" in {
      val id = 2222
      val select = selectByRequest(id)
      when(repository.select(id)) thenReturn Future.successful(None)

      select ~> Route.seal(routes) ~> check {
        failedRequestCheck
      }
    }
  }

  "existingTypeofAccounts" should {
    "return a type of account list of 2 test and current" in {
      val request = existingTypeofAccountsRequest
      when(repository.existingAccountTypes) thenReturn Future.successful(Seq("current", "test"))

      request ~> routes ~> check {
        commonChecks
        val response = entityAs[AccountTypes].accountTypes
        response.length shouldEqual 2
        response contains "test"
        response contains "current"
      }
    }
  }
  "isConfiguredForImports" should {
    "return 200 and true when account is configured for import" in {

      val accountId = 2
      val expectedIsConfigured = ImportStatus(accountId, Some(true))
      val request = isConfiguredForImportsRequest(accountId)
      when(repository.select(accountId)) thenReturn Future(Some(geneva))

      request ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = entityAs[ImportStatus]
        response shouldBe expectedIsConfigured
      }
    }
    "return 200 and false when account is not configured for import" in {
      val accountId = 5
      val expectedIsConfigured = ImportStatus(accountId, Some(false))
      val request = isConfiguredForImportsRequest(accountId)
      when(repository.select(accountId)) thenReturn Future(Some(brussels))

      request ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = entityAs[ImportStatus]
        response shouldBe expectedIsConfigured
      }
    }
    "return 200 and None when account does not exist" in {
      val accountId = 500
      val expectedIsConfigured = ImportStatus(accountId, None)
      val request = isConfiguredForImportsRequest(accountId)
      when(repository.select(accountId)) thenReturn Future(None)

      request ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val response = entityAs[ImportStatus]
        response shouldBe expectedIsConfigured
      }
    }
  }

  "health" should {
    "return 200 with status UP when DB is reachable" in {
      val request = healthRequest
      when(repository.healthCheck) thenReturn Future.successful(true)
      request ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        val response = entityAs[HealthStatus]
        response.status shouldEqual "UP"
        response.db shouldEqual "UP"
        response.buildInfo should contain key "version"
        response.uptimeMs should be >= 0L
      }
    }
    "return 503 with status DOWN when DB is unreachable" in {
      val request = healthRequest
      when(repository.healthCheck) thenReturn Future.successful(false)
      request ~> routes ~> check {
        status shouldEqual StatusCodes.ServiceUnavailable
        val response = entityAs[HealthStatus]
        response.status shouldEqual "DOWN"
        response.db shouldEqual "DOWN"
      }
    }
  }
  "metrics" should {
    "return 200 with counters and timers" in {
      val request = metricsRequest
      request ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        val response = entityAs[String]
        response should include("counters")
        response should include("timers")
        response should include("gauges")
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
}
