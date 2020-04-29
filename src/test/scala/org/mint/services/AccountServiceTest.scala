package org.mint.services

import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.mint.Exceptions.InvalidAccount
import org.mint.akkahttp.utils.TestData._
import org.mint.services.AccountService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.Future

class AccountServiceTest extends AsyncWordSpecLike with Matchers with ScalatestRouteTest with ScalaFutures {
  val service = wire[AccountService[Future]]

  "AccountService" should {
    "insert new account and return it's id" in {
      whenReady(service.insert(berlin)) {
        result =>
          val expectedId = 1
          result should equal(expectedId)
      }
    }
    "raise an error when name is null" in {
      val resultException = service.insert(berlinWithNullName)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
    "raise an error when name is empty string" in {
      val resultException = service.insert(berlinWithEmptyName)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
    "raise an error when AccountType is null" in {
      val resultException = service.insert(berlinWithNullAccountType)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
    "raise an error when AccountType is empty string" in {
      val resultException = service.insert(berlinWithEmptyAccountType)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
    "raise an error when company is null" in {
      val resultException = service.insert(berlinWithNullCompany)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
    "raise an error when company is empty string" in {
      val resultException = service.insert(berlinWithEmptyCompany)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
  }
}
