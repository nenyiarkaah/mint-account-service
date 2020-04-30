package org.mint.repositories

import org.mint.models.Account

trait Repository[F[_]] {
  def insert(row: Account): F[Int]
  def createSchema(): F[Unit]
}
