package io.github.hugogu.balance.common.model

import java.io.Serializable
import java.math.BigDecimal

data class AccountUpdateRequest(
    val amount: BigDecimal = BigDecimal.ZERO
) : Serializable
