package io.github.hugogu.balance.transaction.service.error

import java.lang.RuntimeException

class TransactionBusinessError(message: String) : RuntimeException(message)
