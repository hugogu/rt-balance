package io.github.hugogu.balance.common.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class TransactionMessage(
    val transactionId: UUID = UUID(0, 0),
    val fromAccount: UUID = UUID(0, 0),
    val toAccount: UUID = UUID(0, 0),
    val amount: BigDecimal = BigDecimal.ZERO,
    val timestamp: Instant = Instant.EPOCH
)
