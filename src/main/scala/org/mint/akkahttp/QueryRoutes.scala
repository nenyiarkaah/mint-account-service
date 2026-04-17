package org.mint.akkahttp

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.typesafe.scalalogging.StrictLogging
import org.mint.Exceptions.UnknownSortField
import org.mint.info.BuildInfo.toJson
import org.mint.models.{Account, AccountTypes, ImportStatus}
import org.mint.services.AccountService
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import org.mint.json.SprayJsonFormat._

class QueryRoutes (service: AccountService)(
  implicit ec: ExecutionContext,
  system: ActorSystem,
  actt: ToResponseMarshaller[AccountTypes],
  a: ToResponseMarshaller[Account],
  is: ToResponseMarshaller[ImportStatus]
) extends CORSHandler with StrictLogging {

  def prefix(r: Route): Route = pathPrefix("api" / "accounts")(r)

  def routes: Route = corsHandler(prefix(concat(
    selectAllRoute,
    selectByIdRoute,
    existingTypeofAccountsRoute,
    isConfiguredForImportsRoute,
    healthRoute
  )))

  private def selectAllRoute: Route =
    pathEndOrSingleSlash {
      get {
        parameters('sort.?, 'page.as[Int].?, 'pageSize.as[Int].?) { (sort, page, pageSize) =>
          logger.info(s"Select all sorted by '$sort'")
          onComplete(service.selectAll(page, pageSize, sort)) {
            case Success(accounts) => complete(accounts)
            case Failure(UnknownSortField(field)) =>
              complete(StatusCodes.BadRequest, s"Unknown sort field: $field")
            case Failure(t) =>
              complete(StatusCodes.InternalServerError, s"Something bad happened: $t")
          }
        }
      }
    }

  private def selectByIdRoute: Route =
    path(IntNumber) { id =>
      get {
        logger.debug(s"Select account by id: $id")
        rejectEmptyResponse { complete(service.select(id)) }
      }
    }

  private def existingTypeofAccountsRoute: Route =
    path("existingtypeofaccounts") {
      get {
        logger.info("Existing type of accounts")
        complete(service.existingTypeofAccounts)
      }
    }

  private def isConfiguredForImportsRoute: Route =
    path("isconfiguredforimports") {
      get {
        parameter("id") { sId =>
          val id = sId.toInt
          logger.info(s"Account $id is configured for import")
          complete(service.isConfiguredForImports(id))
        }
      }
    }

  private def healthRoute: Route =
    path("health") {
      get {
        logger.info("Health request")
        complete(toJson)
      }
    }
}
