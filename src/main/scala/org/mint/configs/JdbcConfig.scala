package org.mint.configs

import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import org.mint.configs.refined._

final case class JdbcConfig(
                             host: NonEmptyString,
                             port: UserPortNumber,
                             dbName: NonEmptyString,
                             url: JdbcUrl,
                             driver: NonEmptyString,
                             user: NonEmptyString,
                             password: NonEmptyString,
                             connectionTimeout: ConnectionTimeout,
                             maximumPoolSize: MaxPoolSize
                           )
