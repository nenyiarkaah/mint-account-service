package org.mint.services

import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import org.mint.Exceptions.InvalidAccount
import org.mint.models.ImportStatus
import org.mint.repositories.AccountRepository
import org.mint.utils.TestData._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.Future

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
          result shouldEqual expectedId
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

  "select account by id" should {
    "return an account when the id is valid" in {
      val id = 122
      when(mockRepository.select(id)) thenReturn(Future(Some(madrid)))
      whenReady(service.select(id)) {
        result =>
          result shouldBe defined
          result.get shouldEqual madrid
      }
    }
    "return an empty option when the id is invalid" in {
      val id = 1
      when(mockRepository.select(id)) thenReturn(Future(None))
      whenReady(service.select(id)) {
        result =>
          result shouldBe empty
      }
    }

  }

  "existingTypeofAccounts" should {
    "return a distinct list of 2 account types" in {
      when(mockRepository.selectAll) thenReturn(Future(mockData))
      whenReady(service.existingTypeofAccounts) {
        result =>
          val typeofAccounts = result.accountTypes
          val expectedLength = 2
          typeofAccounts.length shouldEqual expectedLength
          typeofAccounts should contain("test")
          typeofAccounts should contain("current")
      }
    }
  }

  "update" should {
    "update existing account with valid name and return it's id" in {
      val givenAndExpectedId = 1
      when(mockRepository.selectAll) thenReturn(Future(Seq(berlin)))
      when(mockRepository.update(berlin.id, berlinWithPrefixedName)) thenReturn Future(givenAndExpectedId)
      whenReady(service.update(berlin.id, berlinWithPrefixedName)) {
        result =>
          result shouldEqual givenAndExpectedId
      }
    }
  }

  "delete" should {
    "delete a valid account and return it's id" in {
      val givenAndExpectedId = 1
      when(mockRepository.delete(givenAndExpectedId)) thenReturn(Future(givenAndExpectedId))
      whenReady(service.delete(givenAndExpectedId)) {
        result =>
          result shouldEqual givenAndExpectedId
      }
    }
    "delete a invalid account returns an error" in {
      val givenId = 1
      val expectedId = 0
      when(mockRepository.delete(givenId)) thenReturn(Future(expectedId))
      whenReady(service.delete(givenId)) {
        result =>
          result shouldEqual expectedId
      }
    }
  }

  "isConfiguredForImports" should {
    "return true when account is configured for import" in {
      val accountId = 1
      val expectedIsConfigured = ImportStatus(Some(true))
      when(mockRepository.select(accountId)) thenReturn (Future(Some(berlin)))
      whenReady(service.IsConfiguredForImports(accountId)) {
        result =>
          result shouldBe expectedIsConfigured
      }
    }
    "return false if the account exists and is not configured for importing" in {
      val accountId = 1
      val expectedIsConfigured = ImportStatus(Some(false))
      when(mockRepository.select(accountId)) thenReturn (Future(Some(brussels)))
      whenReady(service.IsConfiguredForImports(accountId)) {
        result =>
          result shouldBe expectedIsConfigured
      }
    }
    "return false if the account exists and is not my account" in {
      val accountId = 7
      val expectedIsConfigured = ImportStatus(Some(false))
      when(mockRepository.select(accountId)) thenReturn (Future(Some(rome)))
      whenReady(service.IsConfiguredForImports(accountId)) {
        result =>
          result shouldBe expectedIsConfigured
      }
    }
    "return false if the account exists and is not active" in {
      val accountId = 8
      val expectedIsConfigured = ImportStatus(Some(false))
      when(mockRepository.select(accountId)) thenReturn (Future(Some(athens)))
      whenReady(service.IsConfiguredForImports(accountId)) {
        result =>
          result shouldBe expectedIsConfigured
      }
    }
    "return false if the account does not exist" in {
      val accountId = 122
      val expectedIsConfigured = ImportStatus(Some(false))
      when(mockRepository.select(accountId)) thenReturn (Future(None))
      whenReady(service.IsConfiguredForImports(accountId)) {
        result =>
          result shouldBe expectedIsConfigured
      }
    }
  }
}
