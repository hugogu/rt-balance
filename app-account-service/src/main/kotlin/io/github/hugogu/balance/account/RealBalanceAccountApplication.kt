package io.github.hugogu.balance.account

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.retry.annotation.EnableRetry
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableCaching
@EnableRetry
@EnableJpaAuditing
@EnableTransactionManagement
@EnableDubbo
@SpringBootApplication(scanBasePackages = ["io.github.hugogu.balance"])
class RealBalanceAccountApplication

fun main(args: Array<String>) {
    runApplication<RealBalanceAccountApplication>(*args)
}
