package io.github.hugogu.balance.account.facade

import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.NotEmpty
import java.util.Currency

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
data class AccountCreationRequest(
    @field:NotEmpty
    val accountNumber: String = "",
    val currency: Currency,
)
