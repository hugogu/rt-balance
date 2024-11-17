package io.github.hugogu.balance.transaction.client

import java.math.BigDecimal

data class AccountUpdateRequest(
    val amount: BigDecimal = BigDecimal.ZERO
)
