package io.github.hugogu.balance.account.facade

import io.github.hugogu.balance.account.service.AccountService
import io.github.hugogu.balance.common.model.TransactionMessage
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Validated
@RestController("/account")
class AccountController(
    private val accountService: AccountService
) {
    @PostMapping("/account")
    @Transactional
    fun createAccount(
        @RequestBody request: AccountCreationRequest,
        /**
         * Used as idempotent ID
         */
        @RequestHeader("X-Request-ID") requestId: UUID,
    ): AccountIdentity {
        val entity = accountService.createAccount(request.accountNumber, request.currency.currencyCode, requestId)
        return AccountIdentity(entity.id!!)
    }

    @GetMapping("/account/{id}")
    fun queryAccountDetail(@PathVariable id: UUID): AccountDetail {
        val entity = accountService.queryAccountDetail(id)

        return AccountDetail.from(entity)
    }

    @PostMapping("/account:transfer")
    fun processTransaction(@RequestBody transaction: TransactionMessage): AccountDetail {
        val (fromAccount, _) = accountService.processTransaction(transaction)

        return AccountDetail.from(fromAccount)
    }

    /**
     * This API is used to process the transaction message from the message queue.
     * TODO: move it into a standalone service for better separation of concerns.
     */
    @PostMapping("/account:transfer/message")
    fun postTransactionMessage(@RequestBody transaction: TransactionMessage) {
        accountService.postTransaction(transaction)
    }

    /**
     * This API stores the transaction in the database and then processes it asynchronously.
     * people usually think that this is a good practice to improve the performance of the API.
     * But in practice, it would depend on the actual situation where how long the processing time is.
     * In this real time balance system, the processing time is usually very short,
     * so it is not necessary to process it asynchronously.
     *
     * This API is only implemented for demonstration & performance comparison purposes.
     *
     * The benchmark shows this api is 15% slower than the synchronous version with optimal thread pool settings.
     */
    @Deprecated("This API is only implemented for demonstration & performance comparison purposes.")
    @PostMapping("/account:transfer/async")
    fun processTransactionAsync(@RequestBody transaction: TransactionMessage): ResponseEntity<Void> {
        val entity = accountService.captureTransaction(transaction)
        accountService.processTransactionAsync(entity.id!!) { transactionId ->
            accountService.processLoggedTransaction(transactionId)
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    /**
     * TODO: Lock requestID.
     */
    @PostMapping("/account:debit/{id}")
    fun debitAccount(
        @PathVariable id: UUID,
        @RequestBody request: AccountDebitRequest,
        /**
         * Used as idempotent ID
         */
        @RequestHeader("X-Request-ID") requestId: UUID,
    ): AccountDetail {
        val entity = accountService.debitAccount(id, request.amount)

        return AccountDetail.from(entity)
    }

    @PostMapping("/account:credit/{id}")
    fun creditAccount(
        @PathVariable id: UUID,
        @RequestBody request: AccountCreditRequest,
        /**
         * Used as idempotent ID
         */
        @RequestHeader("X-Request-ID") requestId: UUID,
    ): AccountDetail {
        val entity = accountService.creditAccount(id, request.amount)

        return AccountDetail.from(entity)
    }

    // TODO: add more account operations
}
