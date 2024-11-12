package io.github.hugogu.balance.account.service.error

import java.lang.RuntimeException
import java.util.*

class AccountNotFoundException : RuntimeException {
    constructor(accountId: UUID) : super("Account not found: $accountId")
    constructor(message: String) : super(message)
}
