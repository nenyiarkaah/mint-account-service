package org.mint.akkahttp

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import org.mint.models.{Account, AccountTypes, Accounts, ImportStatus}
import org.mint.services.AccountService

import scala.concurrent.{ExecutionContext, Future}

class QueryRoutes (service: AccountService[Future])(
  implicit ec: ExecutionContext,
  system: ActorSystem,
  ts: ToResponseMarshaller[Accounts],
  actt: ToResponseMarshaller[AccountTypes],
  a: ToResponseMarshaller[Account],
  is: ToResponseMarshaller[ImportStatus]
) extends CORSHandler {

  def prefix(r: Route): Route = pathPrefix("api" / "accounts")(r)

  def routes: Route = {
    lazy val log = Logging(system, getClass)

    val route = prefix {
      concat(
        pathEndOrSingleSlash {
          get {
            parameters('sort.?, 'page.as[Int].?, 'pageSize.as[Int].?) { (sort, page, pageSize) =>
              log.debug("Select all sorted by '{}'", sort)
              val accounts = service.selectAll(page, pageSize, sort)
              complete(accounts)
            }
          }
        },
        path(IntNumber) { id =>
          concat(get {
            val maybeAccount = service.select(id)
            log.debug("Found account: {}", maybeAccount)
            rejectEmptyResponse {
              complete(maybeAccount)
            }
          })
        },
        path("existingtypeofaccounts") {
          concat(pathEndOrSingleSlash {
            log.debug("Existing type of accounts")
            val typeOfAccounts = service.existingTypeofAccounts
            complete(typeOfAccounts)
          })
        },
        path("isconfiguredforimports") {
          get {
            parameter("id") { sId =>
              val id = sId.toInt
              val isConfigured = service.IsConfiguredForImports(id)
              log.debug("Account {0} is configured for import: {1}", id, isConfigured)
              complete(isConfigured)
            }
          }
        }
      )
    }
    corsHandler(route)
  }
}
