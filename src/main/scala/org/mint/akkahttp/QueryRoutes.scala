package org.mint.akkahttp

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.typesafe.scalalogging.StrictLogging
import org.mint.info.BuildInfo.toJson
import org.mint.models.{Account, AccountTypes, ImportStatus}
import org.mint.services.AccountService
import scala.concurrent.ExecutionContext
import org.mint.json.SprayJsonFormat._

class QueryRoutes (service: AccountService)(
  implicit ec: ExecutionContext,
  system: ActorSystem,
  actt: ToResponseMarshaller[AccountTypes],
  a: ToResponseMarshaller[Account],
  is: ToResponseMarshaller[ImportStatus]
) extends CORSHandler with StrictLogging {

  def prefix(r: Route): Route = pathPrefix("api" / "accounts")(r)

  def routes: Route = {
    val route = prefix {
      concat(
        pathEndOrSingleSlash {
          get {
            parameters('sort.?, 'page.as[Int].?, 'pageSize.as[Int].?) { (sort, page, pageSize) =>
              logger.info(s"Select all sorted by '$sort'")
              val accounts = service.selectAll(page, pageSize, sort)
              complete(accounts)
            }
          }
        },
        path(IntNumber) { id =>
          concat(get {
            val maybeAccount = service.select(id)
            logger.debug(s"Found account: $maybeAccount")
            rejectEmptyResponse {
              complete(maybeAccount)
            }
          })
        },
        path("existingtypeofaccounts") {
          concat(pathEndOrSingleSlash {
            logger.info("Existing type of accounts")
            val typeOfAccounts = service.existingTypeofAccounts
            complete(typeOfAccounts)
          })
        },
        path("isconfiguredforimports") {
          get {
            parameter("id") { sId =>
              val id = sId.toInt
              val isConfigured = service.isConfiguredForImports(id)
              logger.info(s"Account $id is configured for import: $isConfigured")
              complete(isConfigured)
            }
          }
        },
        path("health") {
          get {
            logger.info("Health request")
            complete(toJson)
          }
        }
      )
    }
    corsHandler(route)
  }
}
