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
import org.mint.configs.FeatureToggles
import org.mint.services.{AccountService, FeatureTogglesService}
import org.mint.json.SprayJsonFormat._

import scala.concurrent.{ExecutionContext, Future}

class AkkaModule(cfg: Config, featureToggles: FeatureToggles)
                (implicit system: ActorSystem, ec: ExecutionContext) extends StrictLogging {

  val db = Database.forConfig("storage", cfg)
  private val featureTogglesService = wire[FeatureTogglesService]
  val accountRepository = wire[AccountRepository]
  val accountService = wire[AccountService]
  val routes = concat(wire[CommandRoutes].routes, wire[QueryRoutes].routes)
  def init(): Future[Unit] = createSchema(featureTogglesService.createSchemaIsEnabled)

  def close(): Unit = db.close()

  def createSchema(isEnabled: Boolean): Future[Unit] = {
    if (isEnabled) {
      accountRepository.createSchema()
    }
    else {
      Future.successful()
    }
  }
}
