package org.mint.akkahttp

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import org.mint.json.GenericJsonWriter
import org.mint.models.Accounts
import org.mint.services.AccountService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class QueryRoutes (service: AccountService[Future])(
  implicit ec: ExecutionContext,
  system: ActorSystem,
  w: GenericJsonWriter[Accounts],
  ts: ToResponseMarshaller[Accounts]
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
        }
      )
    }
    corsHandler(route)
  }
}
