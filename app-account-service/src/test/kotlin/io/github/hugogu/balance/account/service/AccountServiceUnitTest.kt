package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.repo.AccountEntity
import io.github.hugogu.balance.account.repo.AccountRepo
import io.github.hugogu.balance.account.repo.ProcessingStatus
import io.github.hugogu.balance.account.repo.TransactionLogEntity
import io.github.hugogu.balance.account.repo.TransactionLogRepo
import io.github.hugogu.balance.common.model.TransactionMessage
import jakarta.persistence.EntityNotFoundException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.ValueOperations
import org.springframework.kafka.core.KafkaOperations
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.time.Duration
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
class AccountServiceUnitTest {
    private lateinit var accountService: AccountService

    @MockBean
    private lateinit var accountRepo: AccountRepo

    @MockBean
    private lateinit var transactionLog: TransactionLogRepo

    @MockBean
    private lateinit var redisOperations: RedisOperations<String, String>

    @MockBean
    private lateinit var valueOperations: ValueOperations<String, String>

    @MockBean
    private lateinit var kafkaOperations: KafkaOperations<String, TransactionMessage>

    private lateinit var transactionProcessor: TransactionProcessor

    @BeforeEach
    fun setupMock() {
        whenever(redisOperations.opsForValue()).thenReturn(valueOperations)
        whenever(accountRepo.save(any())).thenAnswer { it.arguments[0] }
        whenever(transactionLog.save(any())).thenAnswer { it.arguments[0] }
        whenever(valueOperations.setIfAbsent(any(), any(), any())).thenReturn(true)
        whenever(kafkaOperations.send(any(), any(), any())).thenAnswer {
            transactionProcessor.onReceivingPendingTransaction(it.arguments[2] as TransactionMessage)
            CompletableFuture.completedFuture(null)
        }

        accountService = AccountService(
            accountRepo,
            transactionLog,
            redisOperations,
            kafkaOperations,
            Duration.ofSeconds(10)
        )
        transactionProcessor = TransactionProcessor(accountService, SyncTaskExecutor())
    }

    @Test
    fun createAccountTest() {
        val requestId = UUID.randomUUID()
        val accountEntity = accountService.createAccount("", "USD", BigDecimal("1000000"), requestId)

        assertEquals(requestId, accountEntity.id)
    }

    @Test
    fun queryAccountDetailTest() {
        val accountId = UUID.randomUUID()
        whenever(accountRepo.findById(eq(accountId)))
            .thenReturn(Optional.of(AccountEntity().apply { setId(accountId) }))

        val accountEntity = accountService.queryAccountDetail(accountId)

        assertEquals(accountId, accountEntity.id)
    }

    @Test
    fun queryAccountDetailNotFoundTest() {
        val accountId = UUID.randomUUID()
        whenever(accountRepo.findById(eq(accountId))).thenReturn(Optional.empty())

        try {
            accountService.queryAccountDetail(accountId)
        } catch (e: EntityNotFoundException) {
            assertThat(e.message, containsString(accountId.toString()))
        }
    }

    @Test
    fun persistAndProcessTransactionTest() {
        val (fromAccount, toAccount) = buildAccountPair()
        val transaction = TransactionMessage(UUID.randomUUID(), fromAccount.id!!, toAccount.id!!, BigDecimal.TEN)
        whenever(accountRepo.findAllById(any())).thenReturn(listOf(fromAccount, toAccount))

        val (from, to) = accountService.persistAndProcessTransaction(transaction, accountService::processTransaction)

        assertEquals(from, fromAccount)
        assertEquals(to, toAccount)
        assertEquals(BigDecimal.ZERO, from.balance)
        assertEquals(BigDecimal("20"), to.balance)
    }

    @Test
    fun postTransactionMessageToBrokerTest() {
        val (fromAccount, toAccount) = buildAccountPair()
        val transaction = TransactionMessage(UUID.randomUUID(), fromAccount.id!!, toAccount.id!!, BigDecimal.TEN)
        val transactionEntity = TransactionLogEntity.from(transaction)
        whenever(accountRepo.findAllById(any())).thenReturn(listOf(fromAccount, toAccount))
        whenever(transactionLog.findById(eq(transaction.transactionId)))
            .thenReturn(Optional.of(transactionEntity))

        accountService.postTransactionMessageToBroker(transaction)

        assertEquals(ProcessingStatus.SUCCEED, transactionEntity.status)
    }

    @Test
    fun debitAccountTest() {
        val accountId = UUID.randomUUID()
        val account = AccountEntity().apply {
            setId(accountId)
            balance = BigDecimal.TEN
        }
        whenever(accountRepo.findById(eq(accountId))).thenReturn(Optional.of(account))

        val updatedAccount = accountService.debitAccount(accountId, BigDecimal.ONE, UUID.randomUUID())

        assertEquals(BigDecimal("9"), updatedAccount.balance)
    }

    @Test
    fun creditAccountTest() {
        val accountId = UUID.randomUUID()
        val account = AccountEntity().apply {
            setId(accountId)
            balance = BigDecimal.TEN
        }
        whenever(accountRepo.findById(eq(accountId))).thenReturn(Optional.of(account))

        val updatedAccount = accountService.creditAccount(accountId, BigDecimal.ONE, UUID.randomUUID())

        assertEquals(BigDecimal("11"), updatedAccount.balance)
    }

    private fun buildAccountPair(): Pair<AccountEntity, AccountEntity> {
        val fromId = UUID.randomUUID()
        val toId = UUID.randomUUID()
        val fromAccount = AccountEntity().apply {
            setId(fromId)
            balance = BigDecimal.TEN
        }
        val toAccount = AccountEntity().apply {
            setId(toId)
            balance = BigDecimal.TEN
        }
        return fromAccount to toAccount
    }
}

