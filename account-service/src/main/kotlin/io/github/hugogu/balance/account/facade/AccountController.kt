package io.github.hugogu.balance.account.facade

import io.github.hugogu.balance.account.service.AccountService
import io.github.hugogu.balance.common.model.TransactionMessage
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
    fun processTransaction(@RequestBody transaction: TransactionMessage) {
        accountService.processTransaction(transaction)
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
