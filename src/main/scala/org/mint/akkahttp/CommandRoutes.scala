package org.mint.akkahttp

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, entity, pathEndOrSingleSlash, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import org.mint.Exceptions.InvalidAccount
import org.mint.json.GenericJsonWriter
import org.mint.models.{Account, CommandResult}
import org.mint.services.AccountService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CommandRoutes(service: AccountService[Future])(
  implicit ec: ExecutionContext,
  system: ActorSystem,
  w: GenericJsonWriter[CommandResult],
  t: FromRequestUnmarshaller[Account]
) extends CORSHandler {

  def prefix(r: Route): Route = pathPrefix("api" / "accounts")(r)

  def routes: Route = {
      lazy val log = Logging(system, getClass)

    val route = prefix {
      pathEndOrSingleSlash {
        post {
          entity(as[Account]) { account =>
            log.debug("Create new account '{}'", account)
            val inserted = service.insert(account)
            complete {
              toCommandResponse(inserted, CommandResult)
            }
          }
        }
      }
    }
        corsHandler(route)
  }

    private def toCommandResponse[T](
                                      count: Future[Int],
                                      f: Int => T
                                    )(implicit w: GenericJsonWriter[T], ec: ExecutionContext): Future[HttpResponse] =
    count.transformWith {
      case Success(i) =>
        val e = HttpEntity(ContentTypes.`application/json`, w.toJsonString(f(i)))
        Future.successful(HttpResponse(StatusCodes.OK, entity = e))

      case Failure(e) =>
        val (status, msg) = e match {
          case InvalidAccount(account, m) => (StatusCodes.PreconditionFailed, s"Invalid account: $account. Reason: $m")
          case throwable => (StatusCodes.InternalServerError, s"Something bad happened: $throwable")
        }
        Future.successful(HttpResponse(status, entity = msg))
    }
}
