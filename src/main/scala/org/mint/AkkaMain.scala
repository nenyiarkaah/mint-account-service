package org.mint

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import org.mint.configs.AppConfig
import org.mint.modules.AkkaModule

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

object AkkaMain extends App with StrictLogging {
  implicit val system: ActorSystem = ActorSystem("accounts-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  val (server, _, cfg) =
    AppConfig.load.fold(e => sys.error(s"Failed to load configuration:\n${e.toList.mkString("\n")}"), identity)
  logger.info(s"Server config: $server")

  val mod = new AkkaModule(cfg)
  mod.init().failed.foreach(t => logger.error("Failed to initialize Accounts module", t))

  val serverBinding = Http().bindAndHandle(mod.routes, server.host.value, server.port.value)

  serverBinding.onComplete {
    case Success(b) =>
      logger.info("Server launched at http://{}:{}/", b.localAddress.getHostString, b.localAddress.getPort)
    case Failure(e) =>
      logger.error("Server could not start!", e)
      e.printStackTrace()
      system.terminate()
      mod.close()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
