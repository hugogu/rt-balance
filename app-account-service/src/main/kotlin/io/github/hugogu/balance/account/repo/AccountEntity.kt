package io.github.hugogu.balance.account.repo

import io.github.hugogu.balance.common.EntityBase
import io.github.hugogu.balance.common.model.AccountStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "account")
class AccountEntity : EntityBase() {
    @Column(name = "account_num")
    var accountNumber: String = ""

    /**
     * the ISO 4217 currency code of this currency.
     */
    var accountCcy: String = ""

    /**
     * Weather to store a balance at account entity is one of the most critical design decision in the whole project.
     *
     * Here we assume the balance is often used in many different places and scenarios,
     * so we store it in the account entity for the sake of performance.
     * But it also brings some problems, such as the consistency between account and transaction service.
     */
    var balance: BigDecimal = BigDecimal.ZERO

    var status: AccountStatus = AccountStatus.ACTIVE


    /**
     * This is a typical way to ensure data consistency between account and transaction service.
     * It prevents money lost in the transaction process.
     *
     * Unfortunately, this is not a good way to handle the distributed transaction,
     * it can easily become a bottleneck and a single point failure.
     * It is better to use the Saga pattern for better performance and scalability.
     *
     * So it is not used in this sample project.
     */
    var lockedBalance: BigDecimal = BigDecimal.ZERO

    @LastModifiedDate
    @Column(name = "last_update")
    var lastModified: Instant = Instant.EPOCH

    @Version
    var version: Int = 0
}

