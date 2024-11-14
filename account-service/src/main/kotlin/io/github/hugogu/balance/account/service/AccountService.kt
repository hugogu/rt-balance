package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.repo.*
import io.github.hugogu.balance.common.model.TransactionMessage
import io.github.hugogu.balance.account.service.error.AccountNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

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

    fun processTransaction(transaction: TransactionMessage): Pair<AccountEntity, AccountEntity> {
        val lockKey = "transaction-lock:${transaction.transactionId}"
        val isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS)
        if (isLocked == true) {
            try {
                transactionLogRepo.save(TransactionLogEntity.from(transaction))
                return accountRepo.handleTransaction(transaction)
            } finally {
                redisTemplate.delete(lockKey)
            }
        } else {
            throw IllegalStateException("Transaction is already being processed")
        }
    }

    @Transactional
    fun captureTransaction(transaction: TransactionMessage): TransactionLogEntity {
        return transactionLogRepo.save(TransactionLogEntity.from(transaction))
    }

    @Async
    fun processTransactionAsync(transactionId: UUID, action: Consumer<UUID>) {
        val lockKey = "transaction-lock:${transactionId}"
        val isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS)
        if (isLocked == true) {
            try {
                action.accept(transactionId)
            } finally {
                redisTemplate.delete(lockKey)
            }
        } else {
            throw IllegalStateException("Transaction is already being processed")
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun processLoggedTransaction(transactionId: UUID) {
        val transaction = transactionLogRepo.findByIdOrNull(transactionId)
            ?: throw IllegalStateException("Transaction $transactionId not found")
        try {
            accountRepo.handleTransaction(transaction.transactionData)
            transaction.status = ProcessingStatus.SUCCEED
        } catch (ex: Exception) {
            log.error("Failed to process transaction $transactionId", ex)
            transaction.status = ProcessingStatus.FAILED
        }
    }

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

    companion object {
        private val log = LoggerFactory.getLogger(AccountService::class.java)
    }
}
