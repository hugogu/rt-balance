package io.github.hugogu.balance.account.repo

import io.github.hugogu.balance.common.EntityBase
import io.github.hugogu.balance.common.model.TransactionMessage
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import java.time.Instant

@Entity
@Table(name = "transaction_log")
class TransactionLogEntity : EntityBase() {
    @Type(JsonBinaryType::class)
    @Column(columnDefinition = "jsonb")
    lateinit var transactionData: TransactionMessage

    @Enumerated(EnumType.STRING)
    lateinit var status: ProcessingStatus

    @LastModifiedDate
    @Column(name = "last_update")
    var lastModified: Instant = Instant.EPOCH

    @Version
    var version: Int = 0

    companion object {
        fun from(
            transaction: TransactionMessage,
            status: ProcessingStatus = ProcessingStatus.INIT
        ): TransactionLogEntity {
            return TransactionLogEntity().apply {
                this.setId(transaction.transactionId)
                this.transactionData = transaction
                this.status = status
            }
        }
    }
}
