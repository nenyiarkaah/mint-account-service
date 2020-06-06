package org.mint.services

import org.mint.models.{Account, AccountTypes, Accounts}

trait AccountAlg[F[_]] {
  def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Accounts]
  def selectAll: F[Seq[Account]]
  def insert(account: Account): F[Int]
  def existingTypeofAccounts: F[AccountTypes]
}
