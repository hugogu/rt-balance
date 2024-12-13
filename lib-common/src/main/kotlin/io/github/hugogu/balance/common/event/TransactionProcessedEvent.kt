package io.github.hugogu.balance.common.event

import io.github.hugogu.balance.common.validation.NotZeroUUID
import java.util.UUID

/**
 * Used in request/response style of messaging protocol to inform the result of transaction processing.
 */
data class TransactionProcessedEvent(
    @field:NotZeroUUID
    val transactionId: UUID = UUID(0, 0),
    val result: TransactionProcessStatus = TransactionProcessStatus.COMPLETED,
    val message: String = ""
) {
    companion object {
        const val TOPIC = "events-transaction-processed"
    }
}
