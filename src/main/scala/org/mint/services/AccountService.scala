package org.mint.services

import cats.MonadError
import org.mint.models.Account

import scala.language.higherKinds

class AccountService[F[_]]()(implicit M: MonadError[F, Throwable])  {
  def insert(account: Account): F[Int] =  {
    val id = 1
    M.pure(id)
  }
}
