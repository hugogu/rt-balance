package io.github.hugogu.balance.transaction.facade

import io.github.hugogu.balance.transaction.service.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController("/transaction")
class TransactionController(
    private val transactionService: TransactionService
) {
    @PostMapping("/transaction")
    fun transferSync(
        @RequestBody request: TransferRequest,
        @RequestHeader("X-Request-ID", required = true) requestId: UUID,
    ): ResponseEntity<TransferIdentity> {
        val transaction = transactionService.transfer(
            requestId,
            request.from, request.to,
            request.amount
        )
        transactionService.processTransactionAsync(transaction.id!!)

        return ResponseEntity.status(HttpStatus.CREATED).body(TransferIdentity(transaction.id!!))
    }
}
