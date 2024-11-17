package io.github.hugogu.balance.common.error

import java.lang.RuntimeException
import java.util.*

class AccountNotFoundException(accountId: UUID) : RuntimeException("Account not found: $accountId") {
}
