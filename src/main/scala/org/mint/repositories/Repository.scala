package org.mint.repositories
import scala.concurrent.Future
trait Repository[T] {
  def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[T]]
  def selectAll: Future[Seq[T]]
  def select(id: Int): Future[Option[T]]
  def insert(row: T): Future[Int]
  def createSchema(): Future[Unit]
  def sortingFields: Set[String]
  def update(id: Int, row: T): Future[Int]
  def delete(id: Int): Future[Int]
}
