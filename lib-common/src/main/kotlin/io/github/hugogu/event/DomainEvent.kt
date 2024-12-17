package io.github.hugogu.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
interface DomainEvent {
    /**
     * The timestamp of the event when the event happens.
     *
     * It is NOT intended to be the time when the event is captured, processed or persisted.
     */
    val timestamp: Instant

    /**
     * The identifier of the event. It is used to implements idempotent processing of the events.
     * When an event get published more than once, for retry for instance, this id should be the same.
     *
     * And more generally, any form of reprocess/retry of business logic against history should NOT change the event id.
     */
    val id: UUID
}
