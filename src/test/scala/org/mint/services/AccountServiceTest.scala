package org.mint.services

import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import org.mint.Exceptions.InvalidAccount
import org.mint.repositories.AccountRepository
import org.mint.utils.TestData._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.Future
@Deprecated
class AccountServiceTest extends AsyncWordSpecLike with Matchers with ScalatestRouteTest with ScalaFutures with MockitoSugar {
  val mockRepository = mock[AccountRepository]
  val service = new AccountService[Future](mockRepository)
  when(mockRepository.sortingFields) thenReturn(Set("id", "name"))

  "insert" should {
    "insert new account and return it's id of 1 " in {
      val expectedId = 2
      when(mockRepository.selectAll) thenReturn(Future(mockData))
      when(mockRepository.insert(berlin)) thenReturn(Future(expectedId))
      whenReady(service.insert(berlin)) {
        result =>
          result should equal(expectedId)
      }
    }
    "insert new account and return it's id when there are already existing accounts" in {
      val expectedId = 5
      when(mockRepository.selectAll) thenReturn(Future(Seq(berlin)))
      when(mockRepository.insert(brussels)) thenReturn(Future(expectedId))
      whenReady(service.insert(brussels)) {
        result =>
          result should equal(expectedId)
      }
    }
    "raise an error when account name is null" in {
      val resultException = service.insert(berlinWithNullName)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
    "raise an error when account name is empty string" in {
      val resultException = service.insert(berlinWithEmptyName)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
    "raise an error when account type is null" in {
      val resultException = service.insert(berlinWithNullAccountType)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
    "raise an error when account type is empty string" in {
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
      when(service.selectAll) thenReturn(Future(Seq(madrid)))

      val resultException = service.insert(madrid)
      recoverToSucceededIf[InvalidAccount](resultException)
    }
    "raise an error if an account of the same name already exists and is of a different case" in {
      val resultException = service.insert(madridWithUppercaseName)
      recoverToSucceededIf[InvalidAccount](resultException)
    }

  }

  "selectAll" should {
    "return a list of accounts sorted by id with no null parameters" in {
      val accounts = Seq(paris, madrid, brussels)
      when(mockRepository.selectAll(any[Int], any[Int], any[String])) thenReturn
        Future.successful (accounts.sortBy(_.id))

      whenReady(service.selectAll(None, None, None))
      {
        result =>
          val accounts = result.accounts
          accounts.length shouldEqual 3
          accounts should contain(brussels)
          accounts should contain(paris)
          accounts should contain(madrid)
          accounts shouldEqual Seq(paris, madrid, brussels)
      }
    }
    "return a list of accounts sorted by id" in {
      val accounts = Seq(paris, madrid, brussels)
      when(mockRepository.selectAll(anyInt, anyInt, matches("id"))) thenReturn
        Future.successful (accounts.sortBy(_.id))

      whenReady(service.selectAll(None, None, Some("id")))
      {
        result =>
          val accounts = result.accounts
          accounts.length shouldEqual 3
          accounts shouldEqual Seq(paris, madrid, brussels)
      }
    }
    "return a list of accounts sorted by name" in {
      val accounts = Seq(paris, madrid, brussels)
      when(mockRepository.selectAll(anyInt, anyInt, matches("name"))) thenReturn
        Future.successful (accounts.sortBy(_.name))

      whenReady(service.selectAll(None, None, Some("name")))
      {
        result =>
          val accounts = result.accounts
          accounts.length shouldEqual 3
          accounts shouldEqual Seq(brussels, madrid, paris)
      }
    }
  }
}
