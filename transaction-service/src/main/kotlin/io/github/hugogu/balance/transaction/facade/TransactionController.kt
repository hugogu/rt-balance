package io.github.hugogu.balance.transaction.facade

import io.github.hugogu.balance.transaction.service.TransactionService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Validated
@RestController("/transaction")
class TransactionController(
    private val transactionService: TransactionService
) {
    @PostMapping("/")
    fun transferSync(
        @RequestBody request: TransferRequest,
        @RequestHeader("X-Request-ID", required = false) requestId: UUID? = null,
    ) : TransferIdentity {
        val transaction = transactionService.transfer(requestId ?: UUID.randomUUID(), request.from, request.to, request.amount)
        transactionService.processTransactionAsync(transaction.id!!)

        return TransferIdentity(transaction.id!!)
    }
}
