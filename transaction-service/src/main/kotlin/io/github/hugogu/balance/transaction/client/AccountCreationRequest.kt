package io.github.hugogu.balance.transaction.client

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
data class AccountCreationRequest(
    val accountNumber: String = "",
    val currency: String,
)
