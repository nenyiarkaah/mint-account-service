package org.mint.repositories

import org.mint.models.Account
import slick.jdbc.SQLServerProfile.api._

import scala.concurrent.Future

class AccountRepository(db: Database) extends Repository[Future] {

  class Accounts(tag: Tag) extends Table[Account](tag, "accounts") {
    def id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name: Rep[String] = column[String]("name")
    def accountType: Rep[String] = column[String]("accountType")
    def company: Rep[String] = column[String]("company")
    def myAccount: Rep[Boolean] = column[Boolean]("myAccount")
    def active: Rep[Boolean] = column[Boolean]("active")
    def mappingFile: Rep[String] = column[String]("mappingFile")

    def *  =
      (id, name, accountType, company, myAccount, active, mappingFile) <>
        (Account.tupled, Account.unapply)
  }

  val accounts = TableQuery[Accounts]

  override def insert(acc: Account): Future[Int] = db.run(accounts += acc)
}
