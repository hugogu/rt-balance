package io.github.hugogu.balance.account.facade

import io.github.hugogu.balance.account.repo.AccountEntity
import java.math.BigDecimal
import java.util.*

data class AccountDetail(
    val id: UUID = UUID(0, 0),
    val accountNumber: String = "",
    val accountCurrency: Currency = Currency.getInstance("USD"),
    val balance: BigDecimal = BigDecimal.ZERO,
    val totalLocked: BigDecimal = BigDecimal.ZERO,
    val status: String = "",
) {
    companion object {
        fun from(account: AccountEntity): AccountDetail {
            return AccountDetail(
                id = requireNotNull(account.id),
                accountNumber = account.accountNumber,
                accountCurrency = Currency.getInstance(account.accountCcy),
                balance = account.balance,
                totalLocked = account.lockedBalance,
                status = account.status.name,
            )
        }
    }
}
