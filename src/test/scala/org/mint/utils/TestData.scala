package org.mint.utils

import org.mint.models.{Account, Accounts}

object TestData {
  val accountId = 1
  val berlin = Account(1, "berlin", "current", "Deutsche Bank", true, true, "mapping file")
  val geneva: Account = Account(2, "geneva", "current", "Credit Suisse", true, true, "mapping file")
  val paris: Account = Account(3, "paris", "test", "Banque National Paris", true, false, "mapping file")
  val madrid: Account = Account(4, "madrid", "test", "Banco Mare Nostrun", true, false, "mapping file")
  val brussels: Account = Account(5, "brussels", "test", "Crédit Agricole Group", true, false, "mapping file")

  val berlinWithNullName = Account(1, nullValue, "current", "Deutsche Bank", true, true, "mapping file")
  val berlinWithEmptyName = Account(1, "", "current", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullAccountType = Account(1, "berlin", nullValue, "Deutsche Bank", true, true, "mapping file")
  val berlinWithEmptyAccountType = Account(1, "berlin", "", "Deutsche Bank", true, true, "mapping file")
  val berlinWithNullCompany = Account(1, "berlin", "current", nullValue, true, true, "mapping file")
  val berlinWithEmptyCompany = Account(1, "berlin", "current", "", true, true, "mapping file")
  val berlinWithPrefixedName:Account = Account(2, "prefixberlin", "current", "Deutsche Bank", true, true, "mapping file")

  val madridWithUppercaseName: Account = Account(4, "MADRID", "test", "Banco Mare Nostrun", true, false, "mapping file")
  val mockData: IndexedSeq[Account] =
    IndexedSeq(
      geneva,
      paris,
      madrid
    )
  val newAccountsInstance = Seq()
  val accounts = Seq(geneva, paris, madrid)
  private val nullValue: Null = null
}
