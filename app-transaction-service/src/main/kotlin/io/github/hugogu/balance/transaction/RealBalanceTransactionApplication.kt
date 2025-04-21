package io.github.hugogu.balance.transaction

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@EnableDubbo(scanBasePackages = ["io.github.hugogu.balance.common"])
@SpringBootApplication(scanBasePackages = ["io.github.hugogu.balance"])
class RealBalanceTransactionApplication

fun main(args: Array<String>) {
    runApplication<RealBalanceTransactionApplication>(*args)
}

