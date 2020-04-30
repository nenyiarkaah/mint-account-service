package org.mint.services

import org.mint.models.Account

trait AccountAlg[F[_]] {
def insert(account: Account): F[Int]
}
