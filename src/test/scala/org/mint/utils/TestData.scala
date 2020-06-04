package org.mint.utils

import org.mint.models.{Account, Accounts}

object TestData {
  val accountId = 1
  val berlin = Account(1, "berlin", "test", "Deutsche Bank", true, true, "mapping file")
  val geneva: Account = Account(2, "geneva", "test", "Credit Suisse", true, true, "mapping file")
  val paris: Account = Account(3, "paris", "test", "Banque National Paris", true, false, "mapping file")
  val madrid: Account = Account(4, "madrid", "test", "Banco Mare Nostrun", true, false, "mapping file")
  val brussels: Account = Account(5, "brussels", "test", "Crédit Agricole Group", true, false, "mapping file")

  val berlinWithNullName = Account(1, nullValue, "test", "Deutsche Bank", true, true, "mapping file")
  val berlinWithEmptyName = Account(1, "", "test", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullAccountType = Account(1, "berlin", nullValue, "Deutsche Bank", true, true, "mapping file")
  val berlinWithEmptyAccountType = Account(1, "berlin", "", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullCompany = Account(1, "berlin", "test", nullValue, true, true, "mapping file")
  val berlinWithEmptyCompany = Account(1, "berlin", "test", "", true, true, "mapping file")
  val berlinWithUppercaseName: Account =
    Account(1, "BERLIN", "test", "Deutsche Bank", true, true, "mapping file")

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
  val mockDataForEndToEndSecondary: IndexedSeq[Account] =
    IndexedSeq(
      berlin,
      geneva,
      paris,
      madrid
    )
  val newAccountsInstance = Seq()
  val accounts = Seq(geneva, paris, madrid)
  private val nullValue: Null = null
}
