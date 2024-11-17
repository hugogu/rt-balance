package io.github.hugogu.balance.transaction.service.error

import java.lang.RuntimeException

class TransactionNotFoundException(transactionId: String) :
    RuntimeException("Transaction $transactionId not found") {
}
