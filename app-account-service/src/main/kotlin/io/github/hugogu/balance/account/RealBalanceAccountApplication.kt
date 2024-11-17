package io.github.hugogu.balance.account

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
@SpringBootApplication(scanBasePackages = ["io.github.hugogu.balance"])
class RealBalanceAccountApplication

fun main(args: Array<String>) {
    runApplication<RealBalanceAccountApplication>(*args)
}
