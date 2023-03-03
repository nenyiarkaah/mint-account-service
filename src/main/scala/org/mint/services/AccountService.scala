package org.mint.services

import akka.actor.ActorSystem
import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import org.mint.Exceptions.{InvalidAccount, UnknownSortField}
import org.mint.models.{Account, AccountTypes, ImportStatus}
import org.mint.repositories.{ARepository, Repository}
import org.mint.services.AccountService._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

class AccountService(repo: ARepository)(implicit
                                              M: MonadError[Future,
                                                Throwable],
                                              system: ActorSystem,
                                              ec: ExecutionContext)
  extends Alg[Account] with AlgAccount with StrictLogging {

  private val validateAccount: Account => Future[Account] = {
    case a@Account(_, "", _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'name'"))
    case a@Account(_, null, _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'name'"))
    case a@Account(_, _, "", _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'AccountType'"))
    case a@Account(_, _, null, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'AccountType'"))
    case a@Account(_, _, _, "", _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'company'"))
    case a@Account(_, _, _, null, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'company'"))
    case a@Account(_, _, _, _, _, _) => M.pure((a))
  }

  override def insert(account: Account): Future[Int] = {
      for {
        validatedByFields <- validateAccount(account)
        validatedByName <- validateAccountDoesNotExist(validatedByFields)
        id <- repo.insert(validatedByName)
      } yield id
  }

  override def existingTypeofAccounts: Future[AccountTypes]  = {
    for {
      existingAccounts <- selectAll
    } yield existingTypeofAccounts(existingAccounts)
  }

  override def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): Future[Seq[Account]] = {
    val sortBy = sort
      .map(s => repo.sortingFields.find(_ == s).toRight(UnknownSortField(s)))
      .getOrElse(Right(DefaultSortField))

    M.fromEither(sortBy).flatMap { sort =>
      val page11 = 1

      val pageN = page.getOrElse(DefaultPage)
      val size = pageSize.getOrElse(DefaultPageSize)

      repo
        .selectAll(pageN, size, sort)
//        .map(Accounts)
    }
  }

  override def selectAll: Future[Seq[Account]] = {
    repo.selectAll
  }

  override def select(id: Int): Future[Option[Account]] = repo.select(id)

  override def update(id: Int, account: Account): Future[Int] = {
    for {
      validatedByFields <- validateAccount(account)
      validatedByName <- validateAccountDoesNotExist(validatedByFields)
      id <- repo.update(id, validatedByName)
    } yield id
    repo.update(id, account)
  }

  override def delete(id: Int): Future[Int] = repo.delete(id)

  def isConfiguredForImports(id: Int): Future[ImportStatus] = {
    for {
      act <- repo.select(id)
    } yield act match {
      case None => ImportStatus(id, None)
      case Some(a) => isConfiguredForImports(a)
      case _ => ImportStatus(id, Some(false))
    }
  }

  private def isConfiguredForImports(account: Account): ImportStatus = {
    val isConfiguredForImport = account.isConfiguredForImport
    val isActive = account.isActive
    val statusOpt = Some(isConfiguredForImport && isActive)
    val id = account.id
    ImportStatus(id, statusOpt)
  }

  private def isNotEmpty(x: String) = !(x == null || x.trim.isEmpty)


  private def existingTypeofAccounts(existingAccounts: Seq[Account]) = {
    val accountTypes = existingAccounts.map(_.accountType).distinct
    AccountTypes(accountTypes)
  }

  private def doesAccountNameAlreadyExist(account: Account, accounts: Seq[Account]): Future[Account] = {
    val name = account.name
    val names = getAccountNames(accounts)
    val isNameInAccountsList = names
      .map(_.equalsIgnoreCase(name))
      .foldLeft(false)(_ || _)

    isNameInAccountsList match {
      case false =>
        M.pure(account)
      case _ =>
        M.raiseError(InvalidAccount(account, "completed account already exists"))
    }
  }

  private def getAccountNames(accounts: Seq[Account]): Seq[String] = {
    accounts.map(_.name)
  }

  private def validateAccountDoesNotExist(a: Account): Future[Account] = {
    for {
      existingAccounts <- selectAll
      doesAccountNameAlreadyExist <- doesAccountNameAlreadyExist(a, existingAccounts)
    } yield doesAccountNameAlreadyExist
  }
}

object AccountService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
