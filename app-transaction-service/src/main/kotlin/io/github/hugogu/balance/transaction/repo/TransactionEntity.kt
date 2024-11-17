package io.github.hugogu.balance.transaction.repo

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.hugogu.balance.common.EntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.javamoney.moneta.FastMoney
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount


@Entity
@Table(name = "transaction")
class TransactionEntity : EntityBase() {
    var fromAccount: UUID = UUID(0, 0)

    var toAccount: UUID = UUID(0, 0)

    var currency: String = ""

    @Enumerated(EnumType.STRING)
    var status: TransactionStatus = TransactionStatus.PENDING

    var amount: BigDecimal = BigDecimal.ZERO

    var transactionTime: Instant = Instant.EPOCH

    var settleTime: Instant? = null

    @LastModifiedDate
    @Column(name = "last_update")
    var lastModified: Instant = Instant.EPOCH

    @Version
    var version: Int = 0

    @JsonIgnore
    fun getTransactionAmount(): MonetaryAmount = FastMoney.of(amount, currency)
}
