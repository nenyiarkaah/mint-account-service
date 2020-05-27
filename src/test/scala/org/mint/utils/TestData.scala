package org.mint.utils

import org.mint.models.Account

object TestData {
  val accountId = 1
  val berlin = Account(accountId, "berlin", "test", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullName = Account(accountId, nullValue, "test", "Deutsche Bank", true, true, "mapping file")
  val berlinWithEmptyName = Account(accountId, "", "test", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullAccountType = Account(accountId, "berlin", nullValue, "Deutsche Bank", true, true, "mapping file")
  val berlinWithEmptyAccountType = Account(accountId, "berlin", "", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullCompany = Account(accountId, "berlin", "test", nullValue, true, true, "mapping file")
  val berlinWithEmptyCompany = Account(accountId, "berlin", "test", "", true, true, "mapping file")
  val berlinWithUppercaseName: Account =
    Account(accountId, "BERLIN", "test", "Deutsche Bank", true, true, "mapping file")
  val geneva: Account = Account(2, "geneva", "test", "Credit Suisse", true, true, "mapping file")
  val paris: Account = Account(3, "paris", "test", "Banque National Paris", true, false, "mapping file")
  val madrid: Account = Account(4, "madrid", "test", "Banco Mare Nostrun", true, false, "mapping file")
  val brussels: Account = Account(5, "brussels", "test", "Crédit Agricole Group", true, false, "mapping file")
  val madridWithUppercaseName: Account = Account(4, "MADRID", "test", "Banco Mare Nostrun", true, false, "mapping file")
  val mockData: IndexedSeq[Account] =
    IndexedSeq(
      geneva,
      paris,
      madrid
    )
  val mockDataForEndToEnd: IndexedSeq[Account] =
    IndexedSeq(
      berlin,
      geneva,
      paris
    )
  val newAccountsInstance = Seq()
  val accounts = Seq(geneva, paris, madrid)
  private val nullValue: Null = null
}
