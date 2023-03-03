package org.mint.services

import org.mint.models.AccountTypes
import scala.concurrent.Future
trait AlgAccount {
  def existingTypeofAccounts: Future[AccountTypes]
}
