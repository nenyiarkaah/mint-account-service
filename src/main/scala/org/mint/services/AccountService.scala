package org.mint.services

import akka.actor.ActorSystem
import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.mint.Exceptions.{InvalidAccount, UnknownSortField}
import org.mint.models.Account
import org.mint.repositories.Repository
import org.mint.services.AccountService._
import com.typesafe.scalalogging.StrictLogging

import scala.language.higherKinds

class AccountService[F[_]](repo: Repository[F])(implicit M: MonadError[F, Throwable]) extends AccountAlg[F] with StrictLogging {

  val system: ActorSystem = ActorSystem("accounts-service")

  private val validateAccount: Account => F[Account] = {
    case a@Account(_, "", _, _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'name'"))
    case a@Account(_, null, _, _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'name'"))
    case a@Account(_, _, "", _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'AccountType'"))
    case a@Account(_, _, null, _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'AccountType'"))
    case a@Account(_, _, _, "", _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'company'"))
    case a@Account(_, _, _, null, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'company'"))
    case a@Account(_, _, _, _, _, _, _) => M.pure((a))
  }

  override def insert(account: Account): F[Int] = {
      for {
        validatedByFields <- validateAccount(account)
        validatedByName <- validateAccountDoesNotExist(validatedByFields)
        id <- repo.insert(validatedByName)
      } yield id
  }

  private def validateAccountDoesNotExist(a: Account): F[Account] = {
    val pageSize = 1000
    for {
      existingAccounts <- selectAll(Some(DefaultPage), Some(pageSize), Some(DefaultSortField))
      doesAccountNameAlreadyExist <- doesAccountNameAlreadyExist(a, existingAccounts)
    } yield doesAccountNameAlreadyExist
  }

  override def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Seq[Account]] = {
    val sortBy = sort
      .map(s => repo.sortingFields.find(_ == s).toRight(UnknownSortField(s)))
      .getOrElse(Right(DefaultSortField))

    M.fromEither(sortBy).flatMap { sort =>
      val pageN = page.getOrElse(DefaultPage)
      val size = pageSize.getOrElse(DefaultPageSize)

      repo
        .selectAll(pageN, size, sort)
        .map(Seq[Account])
    }
  }

  private def doesAccountNameAlreadyExist(account: Account, accounts: Seq[Account]): F[Account] = {
    val name = account.name
    val names = getAccountNames(accounts)
    val isNameInAccountsList = names.contains(name)
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
}

object AccountService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
