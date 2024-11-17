package io.github.hugogu.balance.transaction

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@EnableFeignClients
@SpringBootApplication(scanBasePackages = ["io.github.hugogu.balance"])
class RealBalanceTransactionApplication

fun main(args: Array<String>) {
    runApplication<RealBalanceTransactionApplication>(*args)
}

