package org.mint.services

import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.mint.Exceptions.InvalidAccount
import org.mint.models.Account
import org.mint.repositories.Repository
import org.mint.services.AccountService
import org.mint.utils.TestData._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.Future

class AccountServiceTest extends AsyncWordSpecLike with Matchers with ScalatestRouteTest with ScalaFutures {
  val service = wire[AccountService[Future]]

  "insert" should {
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
    "raise an error if an account of the same name already exists" in {
      val resultException = service.insert(madrid)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
  }

  "selectAll" should {
    "return a list of accounts" in {
      val page = Some(1)
      val pageSize = Some(1)
      val sort = Some("id")
      whenReady(service.selectAll(page, pageSize, sort)) {
        result =>
          val expectedResult = accounts
          result should equal(expectedResult)
      }
    }
  }

  private def createStubRepo = {
    new Repository[Future] {
      override def insert(row: Account): Future[Int] = Future.successful(accountId)

      override def createSchema(): Future[Unit] = Future.successful(())

      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Account]] = Future.successful(mockData)

      override def sortingFields: Set[String] = Set("id", "name")
    }
  }
}
