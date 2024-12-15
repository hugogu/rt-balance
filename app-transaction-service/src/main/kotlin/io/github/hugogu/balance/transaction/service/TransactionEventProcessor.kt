package io.github.hugogu.balance.transaction.service

import io.github.hugogu.balance.common.event.TransactionProcessStatus
import io.github.hugogu.balance.common.event.TransactionProcessedEvent
import io.github.hugogu.balance.transaction.repo.TransactionRepo
import io.github.hugogu.balance.transaction.repo.TransactionStatus
import jakarta.persistence.EntityNotFoundException
import jakarta.persistence.PersistenceException
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionEventProcessor(
    private val transactionRepo: TransactionRepo
) {
    @RetryableTopic(
        attempts = "3",
        backoff = Backoff(delay = 1000 * 60, multiplier = 2.0),
        retryTopicSuffix = RETRY_SUFFIX,
        dltTopicSuffix = DLT_SUFFIX,
        numPartitions = "1",
        exclude = [EntityNotFoundException::class, PersistenceException::class]
    )
    @KafkaListener(
        topics = [TransactionProcessedEvent.TOPIC],
        groupId = GROUP_ID,
    )
    @Transactional
    fun onTransactionProcessedEvent(event: TransactionProcessedEvent) {
        val transaction = transactionRepo.findById(event.transactionId).orElseThrow {
            EntityNotFoundException("Can't find transaction ${event.transactionId}")
        }
        if (transaction.status == TransactionStatus.COMPLETED) {
            log.info("Transaction ${event.transactionId} has been processed successfully, skip event: $event")
            return
        }
        transaction.status = when (event.result) {
            TransactionProcessStatus.FAILED -> TransactionStatus.FAILED
            TransactionProcessStatus.COMPLETED -> TransactionStatus.COMPLETED
            TransactionProcessStatus.ACCEPTED -> TransactionStatus.PENDING
        }
        transactionRepo.save(transaction)
    }

    companion object {
        private const val GROUP_ID = "transaction-service-processor"
        private const val RETRY_SUFFIX = "-transaction-retry"
        private const val DLT_SUFFIX = "-transaction-dlt"

        private val log = LoggerFactory.getLogger(TransactionEventProcessor::class.java)
    }
}
