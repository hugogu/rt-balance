package io.github.hugogu.balance.common

import io.github.hugogu.balance.common.model.AccountDetail
import io.github.hugogu.balance.common.model.AccountCreationRequest
import io.github.hugogu.balance.common.model.AccountIdentity
import io.github.hugogu.balance.common.model.TransactionMessage
import io.github.hugogu.balance.common.model.AccountUpdateRequest
import org.apache.dubbo.config.annotation.DubboService
import org.springframework.retry.annotation.Retryable
import java.math.BigDecimal
import java.util.*

@DubboService(version = "1.0.0")
interface AccountServiceClient {

    fun createAccount(
        request: AccountCreationRequest,
        requestId: UUID
    ): AccountIdentity

    fun queryAccountDetail(id: UUID): AccountDetail

    fun processTransaction(transaction: TransactionMessage): AccountDetail

    fun debitAccount(
        accountId: UUID,
        request: AccountUpdateRequest,
        requestId: UUID
    ): AccountDetail

    fun creditAccount(
        accountId: UUID,
        request: AccountUpdateRequest,
        requestId: UUID
    ): AccountDetail

    @Retryable
    fun processTransactionWithRetry(transaction: TransactionMessage): AccountDetail {
        return processTransaction(transaction)
    }

    @Retryable
    fun debitAccountAmount(accountId: UUID, amount: BigDecimal, transactionRequestId: UUID): AccountDetail {
        val request = AccountUpdateRequest(amount)
        val requestId = UUID.nameUUIDFromBytes((transactionRequestId.toString() + accountId.toString()).toByteArray())

        return debitAccount(accountId, request, requestId)
    }

    @Retryable
    fun creditAccountAmount(accountId: UUID, amount: BigDecimal, transactionRequestId: UUID): AccountDetail {
        val request = AccountUpdateRequest(amount)
        val requestId = UUID.nameUUIDFromBytes((transactionRequestId.toString() + accountId.toString()).toByteArray())

        return creditAccount(accountId, request, requestId)
    }
}
