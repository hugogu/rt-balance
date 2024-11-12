package io.github.hugogu.balance.account.repo

import io.github.hugogu.balance.common.EntityBase
import io.github.hugogu.balance.common.model.TransactionMessage
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "transaction_log")
class TransactionLogEntity() : EntityBase() {
    @Type(JsonBinaryType::class)
    @Column(columnDefinition = "jsonb")
    lateinit var transactionData: TransactionMessage

    constructor(transactionData: TransactionMessage) : this() {
        this.transactionData = transactionData
        this.setId(transactionData.transactionId)
    }
}
