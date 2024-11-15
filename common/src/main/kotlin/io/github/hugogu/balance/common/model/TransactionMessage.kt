package io.github.hugogu.balance.common.model

import io.github.hugogu.balance.common.validation.NotZeroUUID
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class TransactionMessage(
    @field:NotZeroUUID
    val transactionId: UUID = UUID(0, 0),
    @field:NotZeroUUID
    val fromAccount: UUID = UUID(0, 0),
    @field:NotZeroUUID
    val toAccount: UUID = UUID(0, 0),
    val amount: BigDecimal = BigDecimal.ZERO,
    val timestamp: Instant = Instant.EPOCH
)
