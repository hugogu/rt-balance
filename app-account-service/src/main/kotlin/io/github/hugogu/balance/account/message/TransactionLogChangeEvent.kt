package io.github.hugogu.balance.account.message

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.hugogu.balance.common.model.EventSource

/**
 * TODO: This class could have been a generic class that used to represent change event of any entity.
 * But due to a bug in RetryableTopic that retires was not deserialize correctly,
 * we have to create a new event class for each entity.
 */
data class TransactionLogChangeEvent(
    val before: TransactionLog? = null,
    val after: TransactionLog? = null,
    val source: EventSource = EventSource(),
    /**
     * @see io.debezium.data.Envelope.Operation
     */
    @JsonProperty("op")
    val operation: String = "c",
    val tsMS: Long = 0L,
    val tsUS: Long = 0L,
    val tsNS: Long = 0L,
)
