package io.github.hugogu.balance.common.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.hugogu.balance.common.validation.NotZeroUUID
import io.github.hugogu.event.DomainEvent
import java.time.Instant
import java.util.UUID

/**
 * Used in request/response style of messaging protocol to inform the result of transaction processing.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
data class TransactionProcessedEvent(
    /**
     * @see DomainEvent.id
     */
    @field:NotZeroUUID
    override val id: UUID = UUID(0, 0),
    @field:NotZeroUUID
    val transactionId: UUID = UUID(0, 0),
    val result: TransactionProcessStatus = TransactionProcessStatus.COMPLETED,
    val message: String = "",
    /**
     * Represents the time when the transaction is processed, regardless succeed or failed.
     */
    override val timestamp: Instant = Instant.EPOCH,
): DomainEvent {
    companion object {
        const val TOPIC = "events-transaction-processed"
    }
}
