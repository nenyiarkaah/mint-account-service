package org.mint.services

import org.mint.models.{Account, AccountTypes}

import scala.concurrent.Future

trait Alg[T] {
  def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): Future[Seq[T]]
  def selectAll: Future[Seq[T]]
  def select(id: Int): Future[Option[T]]
  def insert(entity: T): Future[Int]
  def update(id: Int, entity: T): Future[Int]
  def delete(id: Int): Future[Int]
}
