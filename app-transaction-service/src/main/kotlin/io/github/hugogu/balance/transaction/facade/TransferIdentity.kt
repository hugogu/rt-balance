package io.github.hugogu.balance.transaction.facade

import java.util.*

data class TransferIdentity (
    val id: UUID? = null,
    val resourceType: String = "TRANSACTION",
)
