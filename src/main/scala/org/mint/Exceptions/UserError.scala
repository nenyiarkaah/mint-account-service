package org.mint.Exceptions

import org.mint.models.Account

sealed trait UserError extends Exception
case class InvalidAccount(account: Account, msg: String) extends UserError
case class UnknownSortField(field: String) extends UserError




