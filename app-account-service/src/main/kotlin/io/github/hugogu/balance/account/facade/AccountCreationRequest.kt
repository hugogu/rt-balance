package io.github.hugogu.balance.account.facade

import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.util.Currency

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
data class AccountCreationRequest(
    @field:NotEmpty
    val accountNumber: String = "",
    val currency: Currency = Currency.getInstance("USD"),
    @field:PositiveOrZero
    val balance: BigDecimal = BigDecimal.ZERO
)
