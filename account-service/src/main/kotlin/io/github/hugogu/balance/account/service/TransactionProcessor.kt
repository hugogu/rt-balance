package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.config.KafkaConfig
import io.github.hugogu.balance.account.repo.TransactionLogEntity
import io.github.hugogu.balance.common.model.TransactionMessage
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Service

@Service
class TransactionProcessor(
    val accountService: AccountService
) {
    @RetryableTopic(
        attempts = "3",
        backoff = Backoff(delay = 1000 * 60, multiplier = 2.0),
        retryTopicSuffix = "-account-retry",
        dltTopicSuffix = "-account-dlt"
    )
    @KafkaListener(topics = [KafkaConfig.PENDING_TRANSACTION_TOPIC], groupId = "account-service-processor")
    fun onReceivingPendingTransaction(transaction: TransactionMessage) {
        captureMessageAndTriggerProcessing(transaction)
    }

    fun captureMessageAndTriggerProcessing(transaction: TransactionMessage): TransactionLogEntity {
        val entity = accountService.captureTransaction(transaction)
        accountService.processTransactionAsync(entity.id!!) { transactionId ->
            accountService.processLoggedTransaction(transactionId)
        }

        return entity
    }
}
