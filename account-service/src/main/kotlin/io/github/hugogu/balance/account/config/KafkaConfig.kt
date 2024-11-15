package io.github.hugogu.balance.account.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {
    /**
     * For capturing pending transactions that need to be processed to calculate the balance.
     * This topic should have enough partitions to allow for parallel processing of transactions.
     *
     * Messages in this topic should expire after a certain period of time as the transaction volume grows.
     * TODO: make all related settings configurable in yaml.
     *
     */
    @Bean
    fun pendingTransactionTopic() = TopicBuilder.name(PENDING_TRANSACTION_TOPIC)
        .partitions(4)
        .replicas(1)
        .build()

    companion object {
        const val PENDING_TRANSACTION_TOPIC = "pending-transaction"
    }
}
