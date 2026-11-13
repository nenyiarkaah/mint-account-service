package org.mint.configs

import java.io.File

import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConverters._

class ApplicationConfTest extends WordSpec with Matchers {
  private val env = ConfigFactory.parseMap(Map("DB_USERNAME" -> "db-user", "DB_PASSWORD" -> "db-pass").asJava)

  Seq("src/main/resources/application.conf", "src/test/resources/application.conf").foreach { path =>
    path should {
      "read storage credentials from DB_USERNAME and DB_PASSWORD" in {
        val config = env.withFallback(ConfigFactory.parseFile(new File(path))).resolve()
        config.getString("storage.user") shouldEqual "db-user"
        config.getString("storage.password") shouldEqual "db-pass"
      }
    }
  }
}
