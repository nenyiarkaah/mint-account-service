package org.mint.models

final case class Account(
                          id: Int,
                          name: String,
                          accountType: String,
                          company: String,
                          isActive: Boolean,
                          isConfiguredForImport: Boolean
                        )
