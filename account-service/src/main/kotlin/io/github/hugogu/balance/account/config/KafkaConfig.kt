package io.github.hugogu.balance.account.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {
    @Bean
    fun pendingTransactionTopic() = TopicBuilder.name(PENDING_TRANSACTION_TOPIC)
        .partitions(16)
        .replicas(2)
        .build()

    companion object {
        const val PENDING_TRANSACTION_TOPIC = "pending-transaction"
    }
}
