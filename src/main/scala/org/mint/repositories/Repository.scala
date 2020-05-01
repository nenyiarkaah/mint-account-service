package org.mint.repositories

import org.http4s.server.staticcontent.NoopCacheStrategy
import org.mint.models.Account
import slick.dbio.{Effect, NoStream}
import slick.sql.FixedSqlAction

trait Repository[F[_]] {
  def insert(row: Account): F[Int]
  def createSchema(): F[Unit]
}
