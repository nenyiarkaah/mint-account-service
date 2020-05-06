package org.mint.services

import org.mint.models.{Account, Accounts}

trait AccountAlg[F[_]] {
  def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Seq[Account]]
  def insert(account: Account): F[Int]
}
