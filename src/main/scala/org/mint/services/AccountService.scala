package org.mint.services

import akka.actor.ActorSystem
import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.codahale.metrics.MetricRegistry
import com.typesafe.scalalogging.StrictLogging
import org.mint.Exceptions.{InvalidAccount, InvalidPaginationParams, UnknownSortField}
import org.mint.metrics.MetricsRegistry
import org.mint.models.{Account, AccountTypes, ImportStatus}
import org.mint.repositories.{ARepository, Repository}
import org.mint.services.AccountService._

import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import scala.util.{Failure, Success}

class AccountService(repo: ARepository, metricsRegistry: MetricsRegistry)(implicit
                                              M: MonadError[Future,
                                                Throwable],
                                              system: ActorSystem,
                                              ec: ExecutionContext)
  extends Alg[Account] with AlgAccount with StrictLogging {

  private val reg: MetricRegistry = metricsRegistry.registry
  private val insertSuccessCounter = reg.counter("service.insert.success")
  private val insertFailureCounter = reg.counter("service.insert.failure")
  private val updateSuccessCounter = reg.counter("service.update.success")
  private val updateFailureCounter = reg.counter("service.update.failure")
  private val deleteSuccessCounter = reg.counter("service.delete.success")
  private val insertTimer = reg.timer("service.insert.timer")
  private val selectAllTimer = reg.timer("service.selectAll.timer")

  private val validateAccount: Account => Future[Account] = {
    case a@Account(_, "", _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'name'"))
    case a@Account(_, null, _, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'name'"))
    case a@Account(_, _, "", _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'AccountType'"))
    case a@Account(_, _, null, _, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'AccountType'"))
    case a@Account(_, _, _, "", _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'company'"))
    case a@Account(_, _, _, null, _, _) =>
      M.raiseError(InvalidAccount(a, "completed account must have non-empty 'company'"))
    case a@Account(_, _, _, _, _, _) => M.pure((a))
  }

  override def insert(account: Account): Future[Int] = {
    MDC.put("accountName", account.name)
    val ctx = insertTimer.time()
    val result = for {
      validatedByFields <- validateAccount(account)
      validatedByName <- validateAccountDoesNotExist(validatedByFields)
      id <- repo.insert(validatedByName)
    } yield id
    result.andThen {
      case Success(_) => ctx.stop(); insertSuccessCounter.inc(); MDC.remove("accountName")
      case Failure(_) => ctx.stop(); insertFailureCounter.inc(); MDC.remove("accountName")
    }
  }

  override def existingTypeofAccounts: Future[AccountTypes] =
    repo.existingAccountTypes.map(AccountTypes)

  def healthCheck: Future[Boolean] = repo.healthCheck

  override def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): Future[Seq[Account]] = {
    val sortBy = sort
      .map(s => repo.sortingFields.find(_ == s).toRight(UnknownSortField(s)))
      .getOrElse(Right(DefaultSortField))

    val ctx = selectAllTimer.time()
    val result = M.fromEither(sortBy).flatMap { sort =>
      val pageN = page.getOrElse(DefaultPage)
      val size = pageSize.getOrElse(DefaultPageSize)

      if (pageN < 0) { M.raiseError(InvalidPaginationParams(s"page must be >= 0, got $pageN")) }
      else if (size <= 0) { M.raiseError(InvalidPaginationParams(s"pageSize must be > 0, got $size")) }
      else { repo.selectAll(pageN, size, sort) }
    }
    result.andThen { case _ => ctx.stop() }
  }

  override def selectAll: Future[Seq[Account]] = repo.selectAll

  override def select(id: Int): Future[Option[Account]] = {
    MDC.put("accountId", id.toString)
    repo.select(id).andThen { case _ => MDC.remove("accountId") }
  }

  override def update(id: Int, account: Account): Future[Int] = {
    MDC.put("accountId", id.toString)
    val result = for {
      validatedByFields <- validateAccount(account)
      validatedByName <- validateAccountDoesNotExist(validatedByFields)
      updatedId <- repo.update(id, validatedByName)
    } yield updatedId
    result.andThen {
      case Success(_) => updateSuccessCounter.inc(); MDC.remove("accountId")
      case Failure(_) => updateFailureCounter.inc(); MDC.remove("accountId")
    }
  }

  override def delete(id: Int): Future[Int] = {
    MDC.put("accountId", id.toString)
    repo.delete(id).andThen {
      case Success(_) => deleteSuccessCounter.inc(); MDC.remove("accountId")
      case Failure(_) => MDC.remove("accountId")
    }
  }

  def isConfiguredForImports(id: Int): Future[ImportStatus] = {
    for {
      act <- repo.select(id)
    } yield act match {
      case None => ImportStatus(id, None)
      case Some(a) => isConfiguredForImports(a)
    }
  }

  private def isConfiguredForImports(account: Account): ImportStatus = {
    val isConfiguredForImport = account.isConfiguredForImport
    val isActive = account.isActive
    val statusOpt = Some(isConfiguredForImport && isActive)
    val id = account.id
    ImportStatus(id, statusOpt)
  }

  private def doesAccountNameAlreadyExist(account: Account, accounts: Seq[Account]): Future[Account] = {
    val name = account.name
    val names = getAccountNames(accounts)
    val isNameInAccountsList = names
      .map(_.equalsIgnoreCase(name))
      .foldLeft(false)(_ || _)

    isNameInAccountsList match {
      case false =>
        M.pure(account)
      case _ =>
        M.raiseError(InvalidAccount(account, "completed account already exists"))
    }
  }

  private def getAccountNames(accounts: Seq[Account]): Seq[String] = accounts.map(_.name)

  private def validateAccountDoesNotExist(a: Account): Future[Account] = {
    for {
      existingAccounts <- selectAll
      doesAccountNameAlreadyExist <- doesAccountNameAlreadyExist(a, existingAccounts)
    } yield doesAccountNameAlreadyExist
  }
}

object AccountService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
