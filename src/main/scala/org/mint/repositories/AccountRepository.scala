package org.mint.repositories

import org.mint.models.Account
import slick.jdbc.SQLServerProfile.api._
import slick.sql.FixedSqlAction

import scala.concurrent.Future

class AccountRepository(db: Database) extends Repository[Future] {

  val accounts = TableQuery[Accounts]
  private val sorting = Map(
    "id" -> accounts.sortBy(_.id)
    ,"name" -> accounts.sortBy(_.name)
    ,"accountType" -> accounts.sortBy(_.accountType)
    ,"company" -> accounts.sortBy(_.company)
    ,"isMyAccount" -> accounts.sortBy(_.isMyAccount)
  )

  override def insert(acc: Account): Future[Int] = db.run((accounts returning accounts.map(_.id)) += acc)

  override def createSchema(): Future[Unit] = db.run(accounts.schema.create)

  override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Account]] = {
    sorting.get(sort) match {
      case Some(q) => db.run(q.drop(page * pageSize).take(pageSize).result)
      case None => Future.failed(new RuntimeException(s"Unknown sorting field: $sort"))
    }
  }

  override def selectAll: Future[Seq[Account]] = {
      db.run(accounts.result)
  }

  override def select(id: Int): Future[Option[Account]] =
    db.run(accounts.filter(_.id === id).take(1).result.headOption)

  override def sortingFields: Set[String] = sorting.keys.toSet

  override def update(id: Int, row: Account): Future[Int] = db.run(accounts.filter(_.id === id)
    .map(r => (r.name, r.accountType, r.company, r.isMyAccount, r.isActive, r.mappingFile))
    .update((row.name, row.accountType, row.company, row.isMyAccount, row.isActive, row.mappingFile)))

  def dropSchema(): FixedSqlAction[Unit, NoStream, Effect.Schema] = accounts.schema.drop

  class Accounts(tag: Tag) extends Table[Account](tag, "account") {
    def * =
      (id, name, accountType, company, isMyAccount, isActive, mappingFile) <>
        (Account.tupled, Account.unapply)

    def id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)

    def name: Rep[String] = column[String]("name")

    def accountType: Rep[String] = column[String]("account_type")

    def company: Rep[String] = column[String]("company")

    def isMyAccount: Rep[Boolean] = column[Boolean]("is_my_account")

    def isActive: Rep[Boolean] = column[Boolean]("is_active")

    def mappingFile: Rep[String] = column[String]("mapping_file")
  }
}
