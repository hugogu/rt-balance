package io.github.hugogu.balance.transaction.facade

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.math.BigDecimal
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
data class TransferRequest(
    val from: UUID = UUID(0, 0),
    val to: UUID = UUID(0, 0),
    val amount: BigDecimal = BigDecimal.ZERO,
)
