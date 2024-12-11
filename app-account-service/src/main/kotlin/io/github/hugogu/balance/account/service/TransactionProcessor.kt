package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.config.KafkaConfig
import io.github.hugogu.balance.account.message.TransactionLogChangeEvent
import io.github.hugogu.balance.account.repo.TransactionLogEntity
import io.github.hugogu.balance.common.model.TransactionMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME
import org.springframework.core.task.TaskExecutor
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Validated
@Service
class TransactionProcessor(
    val accountService: AccountService,
    @Qualifier(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    private val asyncExecutor: TaskExecutor,
) {
    /**
     * Dead message won't block further message processing and get automatically retries to improve the success rate.
     *
     * This is a Kafka first approach to process transaction messages.
     *
     * TODO: make these retry settings configurable if necessary.
     */
    @RetryableTopic(
        attempts = "3",
        backoff = Backoff(delay = 1000 * 60, multiplier = 2.0),
        retryTopicSuffix = RETRY_SUFFIX,
        dltTopicSuffix = DLT_SUFFIX,
        numPartitions = "1"
    )
    @KafkaListener(
        topics = [KafkaConfig.PENDING_TRANSACTION_TOPIC],
        groupId = GROUP_ID,
        /**
         * The order of message processing will effect the balance history of an account.
         *
         * By the nature of asynchronous processing, the order of message processing is not guaranteed.
         * The processing logic need to handle the out-of-order messages in processing.
         *
         * TODO: 1. A thread affinity strategy base on fromAccount/toAccount may help locality of processing.
         *       2. A time window that orders messages by timestamp may help to reduce the out-of-order messages.
         */
        concurrency = "1"
    )
    fun onReceivingPendingTransaction(transaction: TransactionMessage) {
        captureAndProcessMessage(transaction)
    }

    /**
     * Process transaction log changes from Debezium.
     *
     * This is a DB first approach to process transaction logs.
     */
    @RetryableTopic(
        attempts = "3",
        backoff = Backoff(delay = 1000 * 60, multiplier = 2.0),
        retryTopicSuffix = RETRY_SUFFIX,
        dltTopicSuffix = DLT_SUFFIX,
        numPartitions = "1",
        // These exceptions are not retryable.
        exclude = [jakarta.persistence.EntityNotFoundException::class, java.lang.ClassCastException::class]
    )
    @KafkaListener(
        topics = [KafkaConfig.DEBEZIUM_TRANSACTION_LOG_TOPIC],
        groupId = GROUP_ID,
        concurrency = "1"
    )
    fun onTransactionLogCreated(@Payload change: TransactionLogChangeEvent) {
        if (change.after != null) {
            if (change.operation == "c" && change.after.status == "INIT") {
                accountService.loadAndProcessLoggedTransaction(change.after.id)
            }
        } else {
            logger.warn("Transaction log change event with null after value: {}", change)
        }
    }

    private fun captureAndProcessMessage(transaction: TransactionMessage): TransactionLogEntity {
        val entity = accountService.persistPendingTransactionMessage(transaction)
        // So that we can process the transaction asynchronously and consume kafka messages quicker.
        asyncExecutor.execute {
            accountService.loadAndProcessLoggedTransaction(transaction.transactionId)
        }

        return entity
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TransactionProcessor::class.java)
        private const val GROUP_ID = "account-service-processor"
        private const val RETRY_SUFFIX = "-account-retry"
        private const val DLT_SUFFIX = "-account-dlt"
    }
}
