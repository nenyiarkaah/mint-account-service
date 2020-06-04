package org.mint.repositories

import org.mint.models.{Account, Accounts}

trait Repository[F[_]] {
  def selectAll(page: Int, pageSize: Int, sort: String): F[Seq[Account]]
  def selectAll: F[Seq[Account]]
  def insert(row: Account): F[Int]
  def createSchema(): F[Unit]
  def sortingFields: Set[String]
}
