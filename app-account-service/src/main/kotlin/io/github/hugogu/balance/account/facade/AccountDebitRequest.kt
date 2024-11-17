package io.github.hugogu.balance.account.facade

import java.math.BigDecimal

data class AccountDebitRequest(
    val amount: BigDecimal = BigDecimal.ZERO
)
