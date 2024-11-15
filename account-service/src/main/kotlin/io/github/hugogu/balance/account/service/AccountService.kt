package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.config.RedisConfig.Companion.ACCOUNT_DETAIL_CACHE
import io.github.hugogu.balance.account.config.KafkaConfig
import io.github.hugogu.balance.account.repo.*
import io.github.hugogu.balance.common.model.TransactionMessage
import jakarta.persistence.EntityNotFoundException
import jakarta.persistence.LockModeType
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.dao.CannotAcquireLockException
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaOperations
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal
import java.time.Duration
import java.util.*
import kotlin.ConcurrentModificationException

typealias AccountPair = Pair<AccountEntity, AccountEntity>

@Service
@Validated
class AccountService(
    private val accountRepo: AccountRepo,
    private val transactionLogRepo: TransactionLogRepo,
    redisOperations: RedisOperations<String, String>,
    private val kafkaOperations: KafkaOperations<String, TransactionMessage>,
    @Value("\${service.lock.timeout}") private val lockTimeout: Duration
) {
    private val valueOperations = redisOperations.opsForValue()

    @Transactional
    fun createAccount(accountNumber: String, accountCcy: String, requestId: UUID): AccountEntity {
        val account = AccountEntity()
        account.setId(requestId)
        account.accountNumber = accountNumber
        account.accountCcy = accountCcy
        return accountRepo.save(account)
    }

    @Cacheable(ACCOUNT_DETAIL_CACHE)
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    fun queryAccountDetail(accountId: UUID): AccountEntity {
        return accountRepo.findById(accountId).orElseThrow {
            EntityNotFoundException("Can't find account $accountId")
        }
    }

    /**
     * TODO: The potential issue of partitioning against transactionId is, transactions of the same account scatter.
     *
     * Database level locking is used to ensure the consistency of the account balance.
     */
    @Retryable
    fun postTransactionMessageToBroker(@Valid transaction: TransactionMessage) {
        kafkaOperations.send(KafkaConfig.PENDING_TRANSACTION_TOPIC, transaction.transactionId.toString(), transaction)
            .exceptionally {
                log.error("Failed to send transaction message to broker", it)
                null
            }
    }

    fun <T> persistAndProcessTransaction(transaction: TransactionMessage, processor: (TransactionMessage) -> T): T {
        return processWithLock(transaction.transactionId) {
            // In synchronous mode, the transaction log is only recorded for reference, not for further processing.
            // So the status upon insertion is just SUCCEED.
            transactionLogRepo.save(TransactionLogEntity.from(transaction, status = ProcessingStatus.SUCCEED))
            processor(transaction)
        }
    }

    @Caching(
        evict = [
            CacheEvict(ACCOUNT_DETAIL_CACHE, key = "{#transaction.fromAccount}"),
            CacheEvict(ACCOUNT_DETAIL_CACHE, key = "{#transaction.toAccount}")
        ]
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Retryable(include = [CannotAcquireLockException::class])
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun processTransaction(@Valid transaction: TransactionMessage): AccountPair {
        val accounts = accountRepo.findAllById(listOf(transaction.fromAccount, transaction.toAccount))
        val from = accounts.find { it.id == transaction.fromAccount }
            ?: throw EntityNotFoundException("Can't find account ${transaction.fromAccount}")
        val to = accounts.find { it.id == transaction.toAccount }
            ?: throw EntityNotFoundException("Can't find account ${transaction.toAccount}")

        from.balance -= transaction.amount
        to.balance += transaction.amount

        return from to to
    }

    @Transactional
    fun persistPendingTransactionMessage(@Valid transaction: TransactionMessage): TransactionLogEntity {
        return transactionLogRepo.save(TransactionLogEntity.from(transaction, status = ProcessingStatus.INIT))
    }

    @Caching(
        evict = [
            CacheEvict(ACCOUNT_DETAIL_CACHE, key = "{#result.fromAccount}"),
            CacheEvict(ACCOUNT_DETAIL_CACHE, key = "{#result.toAccount}")
        ]
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Retryable(include = [CannotAcquireLockException::class])
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun loadAndProcessLoggedTransaction(transactionId: UUID): TransactionMessage {
        return processWithLock(transactionId) {
            val transaction = transactionLogRepo.findByIdOrNull(transactionId)
                ?: throw EntityNotFoundException("Transaction $transactionId not found")
            try {
                processTransaction(transaction.transactionData)
                transaction.status = ProcessingStatus.SUCCEED
            } catch (ex: Exception) {
                log.error("Failed to process transaction $transactionId", ex)
                transaction.status = ProcessingStatus.FAILED
            }
            transaction.transactionData
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun debitAccount(accountId: UUID, amount: BigDecimal): AccountEntity {
        val account = accountRepo.findById(accountId).orElseThrow {
            EntityNotFoundException("Can't find account $accountId")
        }
        account.balance -= amount
        return accountRepo.save(account)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun creditAccount(accountId: UUID, amount: BigDecimal): AccountEntity {
        val account = accountRepo.findById(accountId).orElseThrow {
            EntityNotFoundException("Can't find account $accountId")
        }
        account.balance += amount
        return accountRepo.save(account)
    }

    private fun <T> processWithLock(transactionId: UUID, action: () -> T): T {
        val lockKey = "transaction-lock:${transactionId}"
        val isLocked = valueOperations.setIfAbsent(lockKey, "locked", lockTimeout)
        if (isLocked == true) {
            try {
                return action()
            } finally {
                valueOperations.getAndDelete(lockKey)
            }
        } else {
            throw ConcurrentModificationException("Transaction $transactionId is already being processed")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AccountService::class.java)
    }
}
