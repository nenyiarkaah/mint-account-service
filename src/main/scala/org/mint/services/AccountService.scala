package org.mint.services

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.mint.Exceptions.{InvalidAccount, UnknownSortField}
import org.mint.models.Account
import org.mint.repositories.Repository
import org.mint.services.AccountService._

import scala.language.higherKinds

class AccountService[F[_]](repo: Repository[F])(implicit M: MonadError[F, Throwable]) extends AccountAlg[F] {

  private val validateAccount: Account => F[Unit] = {
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
    case _ => M.pure(())
  }

  override def insert(account: Account): F[Int] = {
    validateAccount(account).flatMap(_ =>
      validateAccountDoesNotExist(account).flatMap(a => {
        a match {
          case true => M.raiseError(InvalidAccount(account, "completed account must have non-empty 'company'"))
          case false => repo.insert(account)
        }
      }
      )
    )
  }

  private def validateAccountDoesNotExist(a: Account): F[Boolean] = {
    val name = a.name
    val accounts = selectAll(Some(DefaultPage), Some(DefaultPageSize), Some(DefaultSortField))
    doesAccountNameAlreadyExist(name, accounts)
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

  private def doesAccountNameAlreadyExist(name: String, accounts: F[Seq[Account]]): F[Boolean] = {
    val names = getAccountNames(accounts)
    names.map(_.contains(name))
  }

  private def getAccountNames(accounts: F[Seq[Account]]): F[Seq[String]] = {
    accounts.map(a => a.map(_.name))
  }
}

object AccountService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
