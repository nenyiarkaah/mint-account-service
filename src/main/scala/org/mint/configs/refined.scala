package org.mint.configs

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.string.MatchesRegex

object refined {
  type ConnectionTimeout = Int Refined Interval.OpenClosed[W.`0`.T, W.`100000`.T]
  type MaxPoolSize = Int Refined Interval.OpenClosed[W.`0`.T, W.`100`.T]
  type JdbcUrl = String Refined MatchesRegex[W.`"""jdbc:[a-z]+://[a-zA-Z0-9-.]+:[0-9]+;databaseName=[a-z-_]+"""`.T]
}
