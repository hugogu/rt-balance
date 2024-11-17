package io.github.hugogu.balance.transaction.test

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor

@Configuration
class TestConfiguration {
    @Bean
    @Primary
    fun syncTaskExecution(): TaskExecutor =
        SyncTaskExecutor()
}
