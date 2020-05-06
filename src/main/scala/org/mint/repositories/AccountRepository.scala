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
    ,"myAccount" -> accounts.sortBy(_.myAccount)
  )

  override def insert(acc: Account): Future[Int] = db.run(accounts += acc)

  override def createSchema(): Future[Unit] = db.run(accounts.schema.create)

  override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Account]] = {
    sorting.get(sort) match {
      case Some(q) => db.run(q.drop(page * pageSize).take(pageSize).result)
      case None => Future.failed(new RuntimeException(s"Unknown sorting field: $sort"))
    }
  }

  override def sortingFields: Set[String] = sorting.keys.toSet

  def dropSchema(): FixedSqlAction[Unit, NoStream, Effect.Schema] = accounts.schema.drop

  class Accounts(tag: Tag) extends Table[Account](tag, "accounts") {
    def * =
      (id, name, accountType, company, myAccount, active, mappingFile) <>
        (Account.tupled, Account.unapply)

    def id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)

    def name: Rep[String] = column[String]("name")

    def accountType: Rep[String] = column[String]("accountType")

    def company: Rep[String] = column[String]("company")

    def myAccount: Rep[Boolean] = column[Boolean]("myAccount")

    def active: Rep[Boolean] = column[Boolean]("active")

    def mappingFile: Rep[String] = column[String]("mappingFile")
  }
}
