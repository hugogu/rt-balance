package io.github.hugogu.balance.account.facade

import io.github.hugogu.balance.account.service.AccountService
import io.github.hugogu.balance.common.model.TransactionMessage
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@Validated
@RestController("/account")
class AccountController(
    private val accountService: AccountService
) {
    @PostMapping("/account")
    @Transactional
    fun createAccount(
        @Valid @RequestBody request: AccountCreationRequest,
        /**
         * Used as idempotent ID
         */
        @RequestHeader("X-Request-ID") requestId: UUID,
    ): ResponseEntity<AccountIdentity> {
        val entity = accountService.createAccount(
            request.accountNumber,
            request.currency.currencyCode,
            request.balance,
            requestId
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AccountIdentity(entity.id!!))
    }

    @GetMapping("/account/{id}")
    fun queryAccountDetail(@PathVariable id: UUID): AccountDetail {
        val entity = accountService.queryAccountDetail(id)

        return AccountDetail.from(entity)
    }

    /**
     * Synchronously process transaction and return the updated account detail.
     */
    @PostMapping("/account:transfer")
    fun processTransaction(@Valid @RequestBody transaction: TransactionMessage): AccountDetail {
        val (fromAccount, _) = accountService.persistAndExecute(transaction.copy(timestamp = Instant.now())) {
            accountService.processTransaction(transaction)
        }

        return AccountDetail.from(fromAccount)
    }

    /**
     * This API is used to process the transaction message from the message queue.
     * The response status 202 (ACCEPTED) to indicate that the message is accepted and will be processed asynchronously.
     *
     * TODO: move it into a standalone service for better separation of concerns.
     */
    @PostMapping("/account:transfer/message")
    fun postTransactionMessage(@Valid @RequestBody transaction: TransactionMessage): ResponseEntity<Void> {
        // Only send to Kafka, then the message will be processed asynchronously.
        accountService.postTransactionMessageToBroker(transaction.copy(timestamp = Instant.now()))

        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
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
     *
     * The transaction will be captured by debezium postgres connector into Kafka.
     * And then the kafka message will be processed by TransactionProcessor.
     *
     * @see io.github.hugogu.balance.account.service.TransactionProcessor.onTransactionLogCreated
     *
     * TODO: the processing logic could & should be moved to a standalone service for better separation of concerns.
     */
    @Deprecated("This API is only implemented for demonstration & performance comparison purposes.")
    @PostMapping("/account:transfer/async")
    fun processTransactionAsync(@Valid @RequestBody transaction: TransactionMessage): ResponseEntity<Void> {
        // Only save into database, then Debezium will capture the change and send it to Kafka.
        accountService.persistPendingTransactionMessage(transaction.copy(timestamp = Instant.now()))

        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    /**
     * TODO: Lock requestID.
     */
    @PostMapping("/account:debit/{id}")
    fun debitAccount(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AccountDebitRequest,
        /**
         * Used as idempotent ID
         */
        @RequestHeader("X-Request-ID") requestId: UUID,
    ): AccountDetail {
        val entity = accountService.debitAccount(id, request.amount, requestId)

        return AccountDetail.from(entity)
    }

    @PostMapping("/account:credit/{id}")
    fun creditAccount(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AccountCreditRequest,
        /**
         * Used as idempotent ID
         */
        @RequestHeader("X-Request-ID") requestId: UUID,
    ): AccountDetail {
        val entity = accountService.creditAccount(id, request.amount, requestId)

        return AccountDetail.from(entity)
    }

    // TODO: add more account operations
}
