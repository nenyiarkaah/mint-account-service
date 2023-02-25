package org.mint.endtoend.utils

import org.mint.models.{Account}

object TestData {
  val berlin = Account(1, "berlin", "current", "Deutsche Bank", true, true)
  val geneva: Account = Account(2, "geneva", "current", "Credit Suisse", true, true)
  val paris: Account = Account(3, "paris", "test", "Banque National Paris", true, false)
  val madrid: Account = Account(4, "madrid", "test", "Banco Mare Nostrun", true, false)
  val copenhagen: Account = Account(5, "copenhagen", "test", "Danmarks Nationalbank", false, true)

  val berlinWithEmptyName = Account(1, "", "current", "Deutsche Bank", true, true)
  val berlinWithNullAccountType = Account(1, "berlin", nullValue, "Deutsche Bank", true, true)
  val berlinWithUppercaseName: Account =
    Account(1, "BERLIN", "test", "Deutsche Bank", true, true)
  val berlinWithPrefixedName:Account = Account(2, "prefixberlin", "current", "Deutsche Bank", true, true)

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
  val mockDataForEndToEndTertiary: IndexedSeq[Account] =
    IndexedSeq(
      berlin,
      geneva,
      paris,
      madrid,
      copenhagen
    )
  private val nullValue: Null = null
}
