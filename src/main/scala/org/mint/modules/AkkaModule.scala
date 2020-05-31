package org.mint.modules

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.concat
import cats.instances.future.catsStdInstancesForFuture
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.mint.repositories.AccountRepository
import slick.jdbc.SQLServerProfile.api._
import com.softwaremill.macwire._
import org.mint.akkahttp.{CommandRoutes, QueryRoutes}
import org.mint.services.AccountService
import org.mint.json.SprayJsonFormat._

import scala.concurrent.{ExecutionContext, Future}

class AkkaModule(cfg: Config)(implicit system: ActorSystem, ec: ExecutionContext) extends StrictLogging {

  val db = Database.forConfig("storage", cfg)
  val repo = wire[AccountRepository]
  val service = wire[AccountService[Future]]
  val routes = concat(wire[CommandRoutes].routes, wire[QueryRoutes].routes)
  def init(): Future[Unit] = repo.createSchema()
  def close(): Unit = db.close()
}
