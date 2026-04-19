package org.mint.akkahttp

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCodes
import java.lang.management.ManagementFactory
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.typesafe.scalalogging.StrictLogging
import org.mint.Exceptions.{InvalidPaginationParams, UnknownSortField}
import org.mint.akkahttp.MDCSupport.withRequestMDC
import org.mint.info.BuildInfo
import org.mint.metrics.{MetricsJsonFormat, MetricsRegistry}
import org.mint.models.{Account, AccountTypes, HealthStatus, ImportStatus}
import org.mint.services.AccountService
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import org.mint.json.SprayJsonFormat._
import MetricsJsonFormat._

class QueryRoutes (service: AccountService, metricsRegistry: MetricsRegistry)(
  implicit ec: ExecutionContext,
  system: ActorSystem,
  actt: ToResponseMarshaller[AccountTypes],
  a: ToResponseMarshaller[Account],
  is: ToResponseMarshaller[ImportStatus],
  hs: ToResponseMarshaller[HealthStatus]
) extends CORSHandler with StrictLogging {

  def prefix(r: Route): Route = pathPrefix("api" / "accounts")(r)

  def routes: Route = corsHandler(prefix(concat(
    selectAllRoute,
    selectByIdRoute,
    existingTypeofAccountsRoute,
    isConfiguredForImportsRoute,
    healthRoute,
    metricsRoute
  )))

  private def selectAllRoute: Route =
    pathEndOrSingleSlash {
      get {
        parameters('sort.?, 'page.as[Int].?, 'pageSize.as[Int].?) { (sort, page, pageSize) =>
          withRequestMDC {
          logger.info(s"Select all sorted by '$sort'")
          onComplete(service.selectAll(page, pageSize, sort)) {
            case Success(accounts) => complete(accounts)
            case Failure(UnknownSortField(field)) =>
              complete(StatusCodes.BadRequest, s"Unknown sort field: $field")
            case Failure(InvalidPaginationParams(msg)) =>
              complete(StatusCodes.BadRequest, s"Invalid pagination parameters: $msg")
            case Failure(t) =>
              complete(StatusCodes.InternalServerError, s"Something bad happened: $t")
          }
          }
        }
      }
    }

  private def selectByIdRoute: Route =
    path(IntNumber) { id =>
      get {
        withRequestMDC {
          logger.debug(s"Select account by id: $id")
          rejectEmptyResponse { complete(service.select(id)) }
        }
      }
    }

  private def existingTypeofAccountsRoute: Route =
    path("existingtypeofaccounts") {
      get {
        withRequestMDC {
          logger.info("Existing type of accounts")
          complete(service.existingTypeofAccounts)
        }
      }
    }

  private def isConfiguredForImportsRoute: Route =
    path("isconfiguredforimports") {
      get {
        parameter("id") { sId =>
          val id = sId.toInt
          withRequestMDC {
            logger.info(s"Account $id is configured for import")
            complete(service.isConfiguredForImports(id))
          }
        }
      }
    }

  private def healthRoute: Route =
    path("health") {
      get {
        withRequestMDC {
          logger.info("Health request")
          onComplete(service.healthCheck) {
            case Success(dbUp) =>
              val uptimeMs = ManagementFactory.getRuntimeMXBean.getUptime
              val buildInfo = Map(
                "name" -> BuildInfo.name,
                "version" -> BuildInfo.version,
                "scalaVersion" -> BuildInfo.scalaVersion,
                "sbtVersion" -> BuildInfo.sbtVersion
              )
              val status = HealthStatus(
                status = if (dbUp) "UP" else "DOWN",
                db = if (dbUp) "UP" else "DOWN",
                buildInfo = buildInfo,
                uptimeMs = uptimeMs
              )
              val httpStatus = if (dbUp) StatusCodes.OK else StatusCodes.ServiceUnavailable
              complete(httpStatus, status)
            case Failure(t) =>
              complete(StatusCodes.ServiceUnavailable, s"DB unavailable: $t")
          }
        }
      }
    }

  private def metricsRoute: Route =
    path("metrics") {
      get {
        withRequestMDC {
        logger.info("Metrics request")
        import spray.json._
        complete(metricsRegistry.registry.toJson.prettyPrint)
        }
      }
    }
}
