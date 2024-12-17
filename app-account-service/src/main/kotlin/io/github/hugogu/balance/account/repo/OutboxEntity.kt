package io.github.hugogu.balance.account.repo

import io.github.hugogu.balance.common.EntityBase
import io.github.hugogu.event.DomainEvent
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import java.time.Instant

/**
 * Reference to [Outbox table definition](https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html)
 */
@Entity
@Table(name = "outbox")
class OutboxEntity : EntityBase() {
    @Column(name = "aggregate_type")
    var aggregateType: String = ""

    @Column(name = "aggregate_id")
    var aggregateId: String = ""

    @Type(JsonBinaryType::class)
    @Column(columnDefinition = "jsonb")
    lateinit var payload: DomainEvent

    var type: String = ""

    @LastModifiedDate
    @Column(name = "last_update")
    var lastModified: Instant = Instant.EPOCH

    @Version
    var version: Int = 0
}
