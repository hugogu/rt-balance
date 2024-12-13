package io.github.hugogu.balance.account.config

import io.github.hugogu.balance.common.event.TransactionProcessedEvent
import org.slf4j.LoggerFactory
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
    /**
     * For capturing pending transactions that need to be processed to calculate the balance.
     * This topic should have enough partitions to allow for parallel processing of transactions.
     *
     * Messages in this topic should expire after a certain period of time as the transaction volume grows.
     */
    @Bean
    fun pendingTransactionTopic() = createTopicWithConfig(PENDING_TRANSACTION_TOPIC).build()

    @Bean
    fun transactionProcessedTopic() = createTopicWithConfig(TransactionProcessedEvent.TOPIC).build()


    private fun createTopicWithConfig(topicName: String): TopicBuilder {
        val topicConfig = creationConfig.topics.getOrElse(topicName) {
            log.error("Can't find topic config for $topicName, fail back to use default config.")
            TopicConfig()
        }
        return TopicBuilder.name(topicName)
            .partitions(topicConfig.partitions)
            .replicas(topicConfig.replicas)
    }

    companion object {
        const val PENDING_TRANSACTION_TOPIC = "commands-transaction-pending"
        const val DEBEZIUM_TRANSACTION_LOG_TOPIC = "changes.account.public.transaction_log"
        private val log = LoggerFactory.getLogger(KafkaConfig::class.java)
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
