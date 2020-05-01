package org.mint.endtoend

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import org.mint.modules.AkkaModule
import org.scalatest.{BeforeAndAfter, DoNotDiscover, Matchers, WordSpec}
import org.mint.utils.TestData._
import org.mint.utils.RequestSupport._
import org.mint.json.SprayJsonFormat._
import org.mint.models.CommandResult

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

  override val container = PostgreSQLContainer("mssql-mint-test")

  lazy val cfg: Config = ConfigFactory.load(
    ConfigFactory
      .parseMap(
        Map(
          "port" -> container.mappedPort(5432),
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

  "Accounts service" should {
    "insert new account" in {
      insertData()
    }
  }

  private def insertData(): Unit =
    mockData.foreach { t =>
      val insert = insertRequest(t)
      insertAndCheck(insert)
    }

  private def insertAndCheck(insert: HttpRequest) = {
    insert ~> mod.routes ~> check {
      if (StatusCodes.OK !== status) println(s"*** Response body: $responseEntity")

      status should ===(StatusCodes.OK)
      contentType should ===(ContentTypes.`application/json`)
      val count = entityAs[CommandResult].count
      count should ===(1)
    }
  }
}
