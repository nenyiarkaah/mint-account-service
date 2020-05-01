package org.mint.utils

import org.mint.models.Account

object TestData {
  val accountId = 1
  private val nullValue: Null = null
  val berlin = Account(accountId, "berlin", "test", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullName = Account(accountId, nullValue, "test", "Deutsche Bank", true, true, "mapping file")
  val berlinWithEmptyName = Account(accountId, "", "test", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullAccountType = Account(accountId, "berlin", nullValue, "Deutsche Bank", true, true, "mapping file")
  val berlinWithEmptyAccountType = Account(accountId, "berlin", "", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullCompany = Account(accountId, "berlin", "test", nullValue, true, true, "mapping file")
  val berlinWithEmptyCompany = Account(accountId, "berlin", "test", "", true, true, "mapping file")

  val mockData: IndexedSeq[Account] =
    IndexedSeq(
      berlin,
      Account(2, "swiss", "test", "Credit Suisse", true, true, "mapping file"),
      Account(3, "paris", "test", "Banque National Paris", true, false, null)
    )
}
