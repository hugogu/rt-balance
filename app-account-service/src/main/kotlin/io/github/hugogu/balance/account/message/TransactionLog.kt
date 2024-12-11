package io.github.hugogu.balance.account.message

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class TransactionLog(
    val id: UUID = UUID(0, 0),
    /**
     * @see io.github.hugogu.balance.account.repo.ProcessingStatus
     */
    val status: String = "",
    @JsonProperty("transaction_data")
    val transactionData: String = "",
    @JsonProperty("create_time")
    val createTimeInUS: Long = 0L,
    @JsonProperty("last_update")
    val lastUpdateInUS: Long = 0L,
    val version: Int = 0
)
