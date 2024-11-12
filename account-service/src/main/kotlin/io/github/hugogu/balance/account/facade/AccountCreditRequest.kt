package io.github.hugogu.balance.account.facade

import java.math.BigDecimal

data class AccountCreditRequest(
    val amount: BigDecimal = BigDecimal.ZERO
)
