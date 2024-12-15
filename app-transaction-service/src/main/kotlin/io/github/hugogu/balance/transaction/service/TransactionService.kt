package io.github.hugogu.balance.transaction.service

import io.github.hugogu.balance.common.model.AccountStatus
import io.github.hugogu.balance.common.model.TransactionMessage
import io.github.hugogu.balance.transaction.client.AccountServiceClient
import io.github.hugogu.balance.transaction.repo.TransactionEntity
import io.github.hugogu.balance.transaction.repo.TransactionRepo
import io.github.hugogu.balance.transaction.repo.TransactionStatus
import io.github.hugogu.balance.transaction.service.error.TransactionBusinessError
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

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
     * TODO: separate Saga status from transaction status by using temporal.io or other workflow engine
     * Another option is to introduce a messaging broker between transaction and account service.
     */
    @Async
    fun processTransactionAsync(transactionId: UUID): TransactionEntity? {
        val transaction = transactionRepo.findById(transactionId).orElseThrow {
            EntityNotFoundException("Can't find transaction $transactionId")
        }
        val transactionMessage = buildMessage(transaction)

        return when (transaction.status) {
            TransactionStatus.PENDING, TransactionStatus.FAILED -> {
                return try {
                    accountServiceClient.processTransactionWithRetry(transactionMessage)
                    transaction
                } catch (e: DataAccessException) {
                    log.error("Failed to process transaction", e)
                    transactionRepo.updateTransactionStatus(transactionId, TransactionStatus.FAILED)
                }
            }
            else -> {
                /* NOOP */
                null
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(TransactionService::class.java)

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
