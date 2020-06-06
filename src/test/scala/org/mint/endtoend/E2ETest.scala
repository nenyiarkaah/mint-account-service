package org.mint.endtoend

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ForAllTestContainer, MSSQLServerContainer}
import com.typesafe.config.{Config, ConfigFactory}
import org.mint.json.SprayJsonFormat._
import org.mint.models.{Account, AccountTypes, Accounts, CommandResult}
import org.mint.modules.AkkaModule
import org.mint.utils.RequestSupport._
import org.mint.utils.TestData._
import org.scalatest.{BeforeAndAfter, DoNotDiscover, Matchers, WordSpec}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

@DoNotDiscover
class E2ETest
  extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfter
  with ForAllTestContainer {

  val container = MSSQLServerContainer("mcr.microsoft.com/mssql/server:2017-latest")

  lazy val cfg: Config = ConfigFactory.load(
    ConfigFactory
      .parseMap(
        Map(
          "name" -> container.dockerImageName,
          "url" -> container.jdbcUrl,
          "user" -> container.username,
          "password" -> container.password
        ).asJava
      )
      .atKey("storage")
  )

  lazy val mod = new AkkaModule(cfg)

  before {
    Await.ready(mod.db.run(mod.repo.dropSchema()), 10.seconds)
    Await.ready(mod.repo.createSchema(), 10.seconds)
  }

  "insert" should {
    "return 200 when inserting new accounts into empty database" in {
      insertData(mockDataForEndToEnd)
    }
    "return a 412 Precondition Failed request when inserting account that is already inserted" in {
      val insert = insertRequest(berlin)
      insertAndCheckSuccessfulRequest(insert, berlin.id)

      val secondInsert = insertRequest(berlin)
      insertAndCheckFailedRequest(secondInsert)
    }
    "return a 412 Precondition Failed request when inserting account that is already inserted but the name has a different case" in {
      val insert = insertRequest(berlin)
      insertAndCheckSuccessfulRequest(insert, berlin.id)

      val secondInsert = insertRequest(berlinWithUppercaseName)
      insertAndCheckFailedRequest(secondInsert)
    }
    "return a 412 Precondition Failed request when inserting account with missing name" in {
      val insert = insertRequest(berlinWithEmptyName)
      insertAndCheckFailedRequest(insert)
    }
  }

  "selectAll" should {
    "return a list of accounts sorted by id" in {
      val request = selectAllRequest
      insertData(mockDataForEndToEnd)

      request ~> mod.routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val accounts = entityAs[Accounts].accounts
        accounts.length shouldEqual 3
        accounts should contain(geneva)
        accounts should contain(paris)
        accounts should contain(berlin)
        accounts shouldEqual Seq(berlin, geneva, paris)
      }
    }
    "return a list of accounts sorted by name" in {
      val page = Some(1)
      val pageSize = Some(1)
      val sort = "name"
      val request = selectAllRequest(sort)
      insertData(mockDataForEndToEndSecondary)

      request ~> mod.routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val accounts = entityAs[Accounts].accounts
        accounts.length shouldEqual 4
        accounts should contain(geneva)
        accounts should contain(paris)
        accounts should contain(berlin)
        accounts should contain(madrid)
        accounts shouldEqual Seq(berlin, geneva, madrid, paris)
      }
    }
  }

  "existingTypeofAccounts" should {
    "return a distinct list of 2 account types" in {
      val request = existingTypeofAccountsRequest
      insertData(mockDataForEndToEndSecondary)

      request ~> mod.routes ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val typeOfAccounts = entityAs[AccountTypes].accountTypes
        typeOfAccounts.length shouldEqual 2
      }
    }
  }

  private def insertData(accounts: IndexedSeq[Account]): Unit =
    accounts.foreach { t =>
      var id = 1
      val insert = insertRequest(t)
      insertAndCheckSuccessfulRequest(insert, t.id)
      id = 1
    }

  private def insertAndCheckSuccessfulRequest(insert: HttpRequest, expectedId: Int): Any = {
    insert ~> mod.routes ~> check {
      val  expectedStatusCode = StatusCodes.OK
      val contentType = ContentTypes.`application/json`
      status shouldEqual expectedStatusCode
      contentType shouldEqual contentType
      val id = entityAs[CommandResult].id
      id shouldEqual expectedId
    }
  }

  private def insertAndCheckFailedRequest(insert: HttpRequest): Any = {
    insert ~> mod.routes ~> check {
      val  expectedStatusCode = StatusCodes.PreconditionFailed
      val contentType = ContentTypes.`text/plain(UTF-8)`
      if (expectedStatusCode !== status) println(s"*** Response body: $responseEntity")

      status shouldEqual expectedStatusCode
      contentType shouldEqual contentType
    }
  }
}
