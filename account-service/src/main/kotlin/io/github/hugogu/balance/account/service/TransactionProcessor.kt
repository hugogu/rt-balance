package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.config.KafkaConfig
import io.github.hugogu.balance.account.repo.TransactionLogEntity
import io.github.hugogu.balance.common.model.TransactionMessage
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME
import org.springframework.core.task.TaskExecutor
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Service

@Service
class TransactionProcessor(
    val accountService: AccountService,
    @Qualifier(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    private val asyncExecutor: TaskExecutor,
) {
    /**
     * Dead message won't block further message processing and get automatically retries to improve the success rate.
     *
     * TODO: make these retry settings configurable if necessary.
     */
    @RetryableTopic(
        attempts = "3",
        backoff = Backoff(delay = 1000 * 60, multiplier = 2.0),
        retryTopicSuffix = "-account-retry",
        dltTopicSuffix = "-account-dlt"
    )
    @KafkaListener(
        topics = [KafkaConfig.PENDING_TRANSACTION_TOPIC],
        groupId = "account-service-processor",
        /**
         * The order of message processing will effect the balance history of an account.
         *
         * By the nature of asynchronous processing, the order of message processing is not guaranteed.
         * The processing logic need to handle the out-of-order messages in processing.
         *
         * TODO: 1. A thread affinity strategy base on fromAccount/toAccount may help locality of processing.
         *       2. A windowed message queue that orders messages by timestamp may help to reduce the out-of-order messages.
         */
        concurrency = "1"
    )
    fun onReceivingPendingTransaction(transaction: TransactionMessage) {
        captureAndProcessMessage(transaction)
    }

    private fun captureAndProcessMessage(transaction: TransactionMessage): TransactionLogEntity {
        val entity = accountService.persistPendingTransactionMessage(transaction)
        // So that we can process the transaction asynchronously and consume kafka messages quicker.
        asyncExecutor.execute {
            accountService.loadAndProcessLoggedTransaction(transaction.transactionId)
        }

        return entity
    }
}
