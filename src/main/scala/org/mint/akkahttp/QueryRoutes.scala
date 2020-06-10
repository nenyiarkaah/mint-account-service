package org.mint.akkahttp

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import org.mint.models.{Account, AccountTypes, Accounts}
import org.mint.services.AccountService

import scala.concurrent.{ExecutionContext, Future}

class QueryRoutes (service: AccountService[Future])(
  implicit ec: ExecutionContext,
  system: ActorSystem,
  ts: ToResponseMarshaller[Accounts],
  actt: ToResponseMarshaller[AccountTypes],
  a: ToResponseMarshaller[Account]
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
        path("existingtypeofaccounts"){
          concat(pathEndOrSingleSlash {
            log.debug("Existing type of accounts")
            val typeOfAccounts = service.existingTypeofAccounts
            complete(typeOfAccounts)
          })
        }
      )
    }
    corsHandler(route)
  }
}
