package io.github.hugogu.balance.account.facade

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.Currency

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
data class AccountCreationRequest(
    val accountNumber: String = "",
    val currency: Currency,
)
