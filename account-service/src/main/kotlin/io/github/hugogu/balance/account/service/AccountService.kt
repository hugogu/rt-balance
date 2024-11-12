package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.common.model.TransactionMessage
import io.github.hugogu.balance.account.repo.AccountEntity
import io.github.hugogu.balance.account.repo.AccountRepo
import io.github.hugogu.balance.account.repo.TransactionLogEntity
import io.github.hugogu.balance.account.repo.TransactionLogRepo
import io.github.hugogu.balance.account.service.error.AccountNotFoundException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class AccountService(
    private val accountRepo: AccountRepo,
    private val transactionLogRepo: TransactionLogRepo,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Transactional
    fun createAccount(accountNumber: String, accountCcy: String, requestId: UUID): AccountEntity {
        val account = AccountEntity()
        account.setId(requestId)
        account.accountNumber = accountNumber
        account.accountCcy = accountCcy
        return accountRepo.save(account)
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    fun queryAccountDetail(accountId: UUID): AccountEntity {
        return accountRepo.findById(accountId).orElseThrow { AccountNotFoundException(accountId) }
    }

    fun processTransaction(transaction: TransactionMessage) {
        val lockKey = "transaction-lock:${transaction.transactionId}"
        val isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS)
        if (isLocked == true) {
            try {
                transactionLogRepo.save(TransactionLogEntity(transaction))
                accountRepo.handleTransaction(transaction)
            } finally {
                redisTemplate.delete(lockKey)
            }
        } else {
            throw IllegalStateException("Transaction is already being processed")
        }
    }

    /**
     * Option 2 for better performance.
     */

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun debitAccount(accountId: UUID, amount: BigDecimal): AccountEntity {
        val account = accountRepo.findById(accountId).orElseThrow { AccountNotFoundException(accountId) }
        account.balance -= amount
        return accountRepo.save(account)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun creditAccount(accountId: UUID, amount: BigDecimal): AccountEntity {
        val account = accountRepo.findById(accountId).orElseThrow { AccountNotFoundException(accountId) }
        account.balance += amount
        return accountRepo.save(account)
    }
}
