package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.config.KafkaConfig
import io.github.hugogu.balance.account.repo.OutboxEntity
import io.github.hugogu.balance.account.repo.OutboxRepo
import io.github.hugogu.balance.common.event.TransactionProcessedEvent
import io.github.hugogu.balance.common.model.TransactionMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaOperations
import org.springframework.stereotype.Service

@Service
class TransactionEventPublisher(
    private val kafkaOperations: KafkaOperations<String, Any>,
    private val outboxRepo: OutboxRepo,
    @Value("\${service.outbox.enabled:false}")
    private val outboxEnabled: Boolean
) {
    @EventListener
    fun publishTransactionProcessedEvent(event: TransactionProcessedEvent) {
        if (outboxEnabled) {
            val outboxEntity = OutboxEntity().apply {
                id = event.id
                aggregateType = "transaction-processed"
                aggregateId = event.transactionId.toString()
                type = TransactionProcessedEvent::class.java.name
                payload = event
            }
            outboxRepo.save(outboxEntity)
        } else {
            kafkaOperations.send(TransactionProcessedEvent.TOPIC, event.transactionId.toString(), event)
                .exceptionally {
                    log.error("Failed to send transaction message to broker", it)
                    null
                }
        }
    }

    @EventListener
    fun publishPendingTransactionMessage(transaction: TransactionMessage) {
        kafkaOperations.send(KafkaConfig.PENDING_TRANSACTION_TOPIC,
            transaction.transactionId.toString(),
            transaction
        ).exceptionally {
            log.error("Failed to send transaction message to broker", it)
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(TransactionEventPublisher::class.java)
    }
}
