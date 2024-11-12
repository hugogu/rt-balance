package io.github.hugogu.balance.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.retry.annotation.EnableRetry
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableRetry
@EnableJpaAuditing
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = ["io.github.hugogu.balance"])
class RealBalanceApplication

fun main(args: Array<String>) {
    runApplication<RealBalanceApplication>(*args)
}
