package io.github.hugogu.balance.account.repo

import io.github.hugogu.balance.account.service.error.AccountNotFoundException
import io.github.hugogu.balance.common.model.TransactionMessage
import jakarta.persistence.LockModeType
import org.springframework.dao.CannotAcquireLockException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface AccountRepo : JpaRepository<AccountEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Retryable(include = [CannotAcquireLockException::class])
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun handleTransaction(transaction: TransactionMessage) {
        val accounts = findAllById(listOf(transaction.fromAccount, transaction.toAccount))
        val from = accounts.find { it.id == transaction.fromAccount } ?: throw AccountNotFoundException(transaction.fromAccount)
        val to = accounts.find { it.id == transaction.toAccount } ?: throw AccountNotFoundException(transaction.toAccount)

        from.balance -= transaction.amount
        to.balance += transaction.amount
    }
}
