package org.mint.services

import cats.MonadError
import cats.syntax.flatMap._
import org.mint.Exceptions.InvalidAccount
import org.mint.models.Account

import scala.language.higherKinds

class AccountService[F[_]]()(implicit M: MonadError[F, Throwable]) {
  def insert(account: Account): F[Int] =  {
    val id = account.id
    validateAccount(account).flatMap(_ => M.pure(account.id))
  }

  private val validateAccount: Account => F[Unit] ={
    case a @ Account(_, "", _, _, _, _, _)  =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'name'"))
    case a @ Account(_, null, _, _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'name'"))
    case a @ Account(_, _, "", _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'AccountType'"))
    case a @ Account(_, _, null, _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'AccountType'"))
    case a @ Account(_, _, _, "", _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'company'"))
    case a @ Account(_, _, _, null, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'company'"))
    case _ => M.pure(())
  }
}
