package org.mint.configs

import eu.timepit.refined.auto._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString

final case class Server(
                         host: NonEmptyString = "localhost",
                         port: UserPortNumber = 8080
                       )

