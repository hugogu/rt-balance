package io.github.hugogu.balance.common.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable
import java.math.BigDecimal

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
data class AccountCreationRequest(
    val accountNumber: String = "",
    val currency: String,
    val balance: BigDecimal = BigDecimal.ZERO
) : Serializable
