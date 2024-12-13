package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.config.KafkaConfig
import io.github.hugogu.balance.common.event.TransactionProcessedEvent
import io.github.hugogu.balance.common.model.TransactionMessage
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionalEventListener

@Service
class TransactionEventPublisher(
    private val kafkaOperations: KafkaOperations<String, Any>,
) {
    @TransactionalEventListener
    fun publishTransactionProcessedEvent(event: TransactionProcessedEvent) {
        kafkaOperations.send(TransactionProcessedEvent.TOPIC, event.transactionId.toString(), event)
            .exceptionally {
                log.error("Failed to send transaction message to broker", it)
                null
            }
    }

    @EventListener
    fun publishPendingTransactionMessage(transaction: TransactionMessage) {
        kafkaOperations.send(KafkaConfig.PENDING_TRANSACTION_TOPIC, transaction.transactionId.toString(), transaction)
            .exceptionally {
                log.error("Failed to send transaction message to broker", it)
                null
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(TransactionEventPublisher::class.java)
    }
}
