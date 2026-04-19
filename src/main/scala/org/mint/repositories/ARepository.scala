package org.mint.repositories

import org.mint.models.Account
import scala.concurrent.Future

trait ARepository extends Repository[Account] {
  def existingAccountTypes: Future[Seq[String]]
  def healthCheck: Future[Boolean]
}
