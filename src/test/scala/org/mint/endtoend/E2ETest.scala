package org.mint.endtoend

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ForAllTestContainer, MSSQLServerContainer}
import com.typesafe.config.{Config, ConfigFactory}
import org.mint.json.SprayJsonFormat._
import org.mint.models.CommandResult
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
      insertData()
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

  private def insertData(): Unit =
    mockDataForEndToEnd.foreach { t =>
      var id = 1
      val insert = insertRequest(t)
      insertAndCheckSuccessfulRequest(insert, t.id)
      id = 1
    }

  private def insertAndCheckSuccessfulRequest(insert: HttpRequest, expectedId: Int): Any = {
    insert ~> mod.routes ~> check {
      val  expectedStatusCode = StatusCodes.OK
      val contentType = ContentTypes.`application/json`
      status should ===(expectedStatusCode)
      contentType should ===(contentType)
      val id = entityAs[CommandResult].id
      id should ===(expectedId)
    }
  }

  private def insertAndCheckFailedRequest(insert: HttpRequest): Any = {
    insert ~> mod.routes ~> check {
      val  expectedStatusCode = StatusCodes.PreconditionFailed
      val contentType = ContentTypes.`text/plain(UTF-8)`
      if (expectedStatusCode !== status) println(s"*** Response body: $responseEntity")

      status should ===(expectedStatusCode)
      contentType should ===(contentType)
    }
  }
}
