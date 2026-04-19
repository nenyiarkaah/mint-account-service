package org.mint.repositories

import com.codahale.metrics.MetricRegistry
import com.typesafe.scalalogging.StrictLogging
import org.mint.metrics.MetricsRegistry
import org.mint.models.Account
import slick.jdbc.SQLServerProfile.api._
import slick.sql.FixedSqlAction

import scala.concurrent.{ExecutionContext, Future}

class AccountRepository(db: Database, metricsRegistry: MetricsRegistry)(implicit ec: ExecutionContext)
  extends ARepository with StrictLogging {

  val accounts = TableQuery[Accounts]
  private val reg: MetricRegistry = metricsRegistry.registry
  private val selectAllTimer = reg.timer("repo.selectAll.timer")
  private val insertTimer = reg.timer("repo.insert.timer")
  private val updateTimer = reg.timer("repo.update.timer")
  private val deleteTimer = reg.timer("repo.delete.timer")
  private val healthCheckTimer = reg.timer("repo.healthCheck.timer")

  private val sorting = Map(
    "id" -> accounts.sortBy(_.id)
    ,"name" -> accounts.sortBy(_.name)
    ,"accountType" -> accounts.sortBy(_.accountType)
    ,"company" -> accounts.sortBy(_.company)
    ,"isActive" -> accounts.sortBy(_.isActive)
    ,"isConfiguredForImport" -> accounts.sortBy(_.isConfiguredForImport)
  )

  override def insert(acc: Account): Future[Int] = {
    val ctx = insertTimer.time()
    db.run((accounts returning accounts.map(_.id)) += acc).andThen { case _ => ctx.stop() }
  }

  override def createSchema(): Future[Unit] = db.run(accounts.schema.create)

  override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Account]] = {
    val ctx = selectAllTimer.time()
    val result = sorting.get(sort) match {
      case Some(q) => db.run(q.drop(page * pageSize).take(pageSize).result)
      case None => Future.failed(new RuntimeException(s"Unknown sorting field: $sort"))
    }
    result.andThen { case _ => ctx.stop() }
  }

  override def selectAll: Future[Seq[Account]] = db.run(accounts.result)

  override def select(id: Int): Future[Option[Account]] =
    db.run(accounts.filter(_.id === id).take(1).result.headOption)

  override def sortingFields: Set[String] = sorting.keys.toSet

  override def update(id: Int, row: Account): Future[Int] = {
    val ctx = updateTimer.time()
    db.run(accounts.filter(_.id === id)
      .map(r => (r.name, r.accountType, r.company, r.isActive, r.isConfiguredForImport))
      .update((row.name, row.accountType, row.company, row.isActive, row.isConfiguredForImport)))
      .andThen { case _ => ctx.stop() }
  }

  override def delete(id: Int): Future[Int] = {
    val ctx = deleteTimer.time()
    db.run(accounts.filter(_.id === id).delete).andThen { case _ => ctx.stop() }
  }

  override def existingAccountTypes: Future[Seq[String]] =
    db.run(accounts.map(_.accountType).distinct.result)

  override def healthCheck: Future[Boolean] = {
    val ctx = healthCheckTimer.time()
    db.run(sql"SELECT 1".as[Int]).map(_.nonEmpty).recover { case _ => false }
      .andThen { case _ => ctx.stop() }
  }

  def dropSchema(): FixedSqlAction[Unit, NoStream, Effect.Schema] = accounts.schema.drop

  class Accounts(tag: Tag) extends Table[Account](tag, "account") {
    def * =
      (id, name, accountType, company, isActive, isConfiguredForImport) <>
        (Account.tupled, Account.unapply)
    def id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def name: Rep[String] = column[String]("name")
    def accountType: Rep[String] = column[String]("account_type")
    def company: Rep[String] = column[String]("company")

    def isActive: Rep[Boolean] = column[Boolean]("is_active")
    def isConfiguredForImport: Rep[Boolean] = column[Boolean]("is_configured_for_import")
  }
}
