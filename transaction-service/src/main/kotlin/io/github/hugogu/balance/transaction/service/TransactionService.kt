package io.github.hugogu.balance.transaction.service

import io.github.hugogu.balance.common.model.AccountStatus
import io.github.hugogu.balance.common.model.TransactionMessage
import io.github.hugogu.balance.transaction.client.AccountServiceClient
import io.github.hugogu.balance.transaction.repo.TransactionEntity
import io.github.hugogu.balance.transaction.repo.TransactionRepo
import io.github.hugogu.balance.transaction.repo.TransactionStatus
import io.github.hugogu.balance.transaction.service.error.TransactionBusinessError
import io.github.hugogu.balance.transaction.service.error.TransactionNotFoundException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.*
@Service
class TransactionService(
    private val transactionRepo: TransactionRepo,
    private val accountServiceClient: AccountServiceClient
) {
    @Transactional
    fun transfer(id: UUID, from: UUID, to: UUID, amount: BigDecimal): TransactionEntity {
        val fromAccount = accountServiceClient.queryAccountDetail(from)
        val toAccount = accountServiceClient.queryAccountDetail(to)
        if (fromAccount.status != AccountStatus.ACTIVE || toAccount.status != AccountStatus.ACTIVE) {
            throw TransactionBusinessError("Account status is not active")
        }
        val transaction = TransactionEntity()
        transaction.setId(id)
        transaction.fromAccount = from
        transaction.toAccount = to
        transaction.amount = amount
        transaction.status = TransactionStatus.PENDING
        transaction.currency = fromAccount.currency.currencyCode
        transaction.transactionTime = Instant.now()

        return transactionRepo.save(transaction)
    }

    /**
     * The workflow saga of the transaction processing.
     * Driven by the transaction status.
     * TODO: separate Saga status from transaction status by using temporal.io or other workflow engine if performance is acceptable.
     * Another option is to introduce a messaging broker between transaction and account service.
     */
    @Async
    fun processTransactionAsync(transactionId: UUID) {
        val transaction = transactionRepo.findById(transactionId).orElseThrow {
            TransactionNotFoundException(transactionId.toString())
        }
        val transactionMessage = buildMessage(transaction)
        when (transaction.status) {
            TransactionStatus.PENDING, TransactionStatus.FAILED -> {
                try {
                    accountServiceClient.processTransactionWithRetry(transactionMessage)
                    transactionRepo.updateTransactionStatus(transactionId, TransactionStatus.COMPLETED)
                } catch (e: Exception) {
                    transactionRepo.updateTransactionStatus(transactionId, TransactionStatus.FAILED)
                }
            }
            else -> {
                /* NOOP */
            }
        }
    }

    companion object {
        fun buildMessage(transaction: TransactionEntity): TransactionMessage {
            return TransactionMessage(
                transaction.id!!,
                transaction.fromAccount,
                transaction.toAccount,
                transaction.amount,
                transaction.transactionTime
            )
        }
    }
}
