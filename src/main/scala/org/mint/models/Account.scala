package org.mint.models

final case class Account(
                          id: Int,
                          name: String,
                          accountType: String,
                          company: String,
                          isMyAccount: Boolean,
                          isActive: Boolean,
                          mappingFile: String
                        )
