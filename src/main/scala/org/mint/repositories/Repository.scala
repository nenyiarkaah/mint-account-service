package org.mint.repositories

import org.mint.models.{Account}

trait Repository[F[_]] {
  def selectAll(page: Int, pageSize: Int, sort: String): F[Seq[Account]]
  def selectAll: F[Seq[Account]]
  def select(id: Int): F[Option[Account]]
  def insert(row: Account): F[Int]
  def createSchema(): F[Unit]
  def sortingFields: Set[String]
  def update(id: Int, row: Account): F[Int]
  def delete(id: Int): F[Int]
}
