package org.mint.configs

import java.io.File

import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions, ConfigRenderOptions}
import com.typesafe.scalalogging.StrictLogging
import pureconfig.{loadConfig, CamelCase, ConfigFieldMapping}
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.ProductHint
import eu.timepit.refined.pureconfig._
import pureconfig.generic.auto._

object AppConfig extends StrictLogging {
  private val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)
  private val renderOptions = ConfigRenderOptions.defaults().setOriginComments(false)

  private val path = sys.env.getOrElse("APP_CONFIG_PATH", "src/main/resources/application.conf")

  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  def load: Either[ConfigReaderFailures, (Server, JdbcConfig, Config)] = {
    val config = ConfigFactory.parseFile(new File(path), parseOptions).resolve()
    logger.debug("config content:\n {}", config.root().render(renderOptions))

    for {
      // validate storage config also
      j <- loadConfig[JdbcConfig](config, "storage")
      c <- loadConfig[Server](config, "server")
    } yield (c, j, config)
  }
}
