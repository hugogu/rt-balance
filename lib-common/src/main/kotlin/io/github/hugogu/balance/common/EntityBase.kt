package io.github.hugogu.balance.common

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class EntityBase : Persistable<UUID> {
    @Id
    private var id: UUID? = null

    @CreatedDate
    var createTime: Instant = Instant.EPOCH

    @Transient
    var new: Boolean = false

    override fun getId(): UUID? {
        return id
    }

    fun setId(id: UUID?) {
        this.id = id
    }

    @JsonIgnore
    override fun isNew(): Boolean {
        return new
    }
}
