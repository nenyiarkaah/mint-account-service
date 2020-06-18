package org.mint.services

import org.mint.models.{Account, AccountTypes, Accounts}

trait AccountAlg[F[_]] {
  def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Accounts]
  def selectAll: F[Seq[Account]]
  def select(id: Int): F[Option[Account]]
  def insert(account: Account): F[Int]
  def existingTypeofAccounts: F[AccountTypes]
  def update(id: Int, account: Account): F[Int]
  def delete(id: Int): F[Int]
}
