package io.github.hugogu.balance.transaction.client

import io.github.hugogu.balance.common.model.TransactionMessage
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.retry.annotation.Retryable
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.*

@FeignClient(name = "account-service", url = "\${account.service.url}")
interface AccountServiceClient {

    @PostMapping("/account")
    fun createAccount(
        @RequestBody request: AccountCreationRequest,
        @RequestHeader("X-Request-ID") requestId: UUID
    ): AccountIdentity

    @GetMapping("/account/{id}")
    fun queryAccountDetail(@PathVariable id: UUID): AccountDetail

    @PostMapping("/account:transfer")
    fun processTransaction(transaction: TransactionMessage)

    @PostMapping("/account:debit/{accountId}")
    fun debitAccount(
        @PathVariable accountId: UUID,
        @RequestBody request: AccountUpdateRequest,
        @RequestHeader("X-Request-ID") requestId: UUID
    ): AccountDetail

    @PostMapping("/account:credit/{accountId}")
    fun creditAccount(
        @PathVariable accountId: UUID,
        @RequestBody request: AccountUpdateRequest,
        @RequestHeader("X-Request-ID") requestId: UUID
    ): AccountDetail

    @Retryable
    fun processTransactionWithRetry(transaction: TransactionMessage) {
        processTransaction(transaction)
    }

    @Retryable
    fun debitAccount(accountId: UUID, amount: BigDecimal, transactionRequestId: UUID): AccountDetail {
        val request = AccountUpdateRequest(amount)
        val requestId = UUID.fromString(transactionRequestId.toString() + accountId.toString())

        return debitAccount(accountId, request, requestId)
    }

    @Retryable
    fun creditAccount(accountId: UUID, amount: BigDecimal, transactionRequestId: UUID): AccountDetail {
        val request = AccountUpdateRequest(amount)
        val requestId = UUID.fromString(transactionRequestId.toString() + accountId.toString())

        return creditAccount(accountId, request, requestId)
    }
}
