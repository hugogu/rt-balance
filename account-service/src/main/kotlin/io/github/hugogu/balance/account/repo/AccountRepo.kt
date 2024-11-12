package io.github.hugogu.balance.account.repo

import io.github.hugogu.balance.account.service.error.AccountNotFoundException
import io.github.hugogu.balance.common.model.TransactionMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface AccountRepo : JpaRepository<AccountEntity, UUID> {

    @Retryable
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun handleTransaction(transaction: TransactionMessage) {
        val from = findById(transaction.fromAccount).orElseThrow {
            AccountNotFoundException(transaction.fromAccount)
        }
        val to = findById(transaction.toAccount).orElseThrow {
            AccountNotFoundException(transaction.toAccount)
        }
        from.balance -= transaction.amount
        to.balance += transaction.amount
    }
}
