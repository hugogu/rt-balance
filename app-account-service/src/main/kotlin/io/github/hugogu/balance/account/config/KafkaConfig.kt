package io.github.hugogu.balance.account.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
@EnableConfigurationProperties(TopicCreationConfig::class)
class KafkaConfig(
    private val creationConfig: TopicCreationConfig
) {
    private val pendingTransactionTopic = creationConfig.topics.getOrDefault(PENDING_TRANSACTION_TOPIC, TopicConfig())

    /**
     * For capturing pending transactions that need to be processed to calculate the balance.
     * This topic should have enough partitions to allow for parallel processing of transactions.
     *
     * Messages in this topic should expire after a certain period of time as the transaction volume grows.
     */
    @Bean
    fun pendingTransactionTopic() = TopicBuilder.name(PENDING_TRANSACTION_TOPIC)
        .partitions(pendingTransactionTopic.partitions)
        .replicas(pendingTransactionTopic.replicas)
        .build()

    companion object {
        const val PENDING_TRANSACTION_TOPIC = "pending-transaction"
    }
}

@ConfigurationProperties(prefix = "kafka")
data class TopicCreationConfig(
    val topics: Map<String, TopicConfig> = emptyMap()
)

data class TopicConfig(
    val name: String = "",
    val partitions: Int = 1,
    val replicas: Int = 1
)
