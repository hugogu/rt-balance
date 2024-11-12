package io.github.hugogu.balance.transaction.client

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.hugogu.balance.common.model.AccountStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class AccountDetail(
    val id: UUID = UUID(0, 0),
    val accountNumber: String = "",
    @JsonProperty("accountCurrency")
    val currency: Currency = Currency.getInstance("USD"),
    val balance: BigDecimal = BigDecimal.ZERO,
    val createTime: Instant = Instant.EPOCH,
    val status: AccountStatus = AccountStatus.ACTIVE,
)
