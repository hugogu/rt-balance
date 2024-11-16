package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.repo.AccountRepo
import io.github.hugogu.balance.common.model.TransactionMessage
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.util.*

@Tag("integration")
@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class AccountServiceCacheIntegrationTest {
    @Autowired
    lateinit var accountService: AccountService

    @SpyBean
    lateinit var accountRepo: AccountRepo

    @Test
    fun queryAccountDetailCacheTest(){
        val account = accountService.createAccount("123456", "USD", UUID.randomUUID())

        // Cache the account details
        accountService.queryAccountDetail(account.id!!)

        verify(accountRepo, times(1)).findById(account.id!!)
        reset(accountRepo)

        // Verify the account details are cached
        accountService.queryAccountDetail(account.id!!)

        verify(accountRepo, never()).findById(account.id!!)
    }

    @Test
    fun processTransactionCacheEvictTest() {
        val accountA = accountService.createAccount("123456", "USD", UUID.randomUUID())
        val accountB = accountService.createAccount("123457", "USD", UUID.randomUUID())
        val transactionMessage = TransactionMessage(
            transactionId = UUID.randomUUID(),
            fromAccount = accountA.id!!,
            toAccount = accountB.id!!,
            amount = BigDecimal("100.00")
        )

        // Cache the account details
        accountService.queryAccountDetail(accountA.id!!)
        accountService.queryAccountDetail(accountB.id!!)

        // Process the transaction would evict the cache
        accountService.processTransaction(transactionMessage)

        // query again would load data from db again.
        accountService.queryAccountDetail(accountA.id!!)
        accountService.queryAccountDetail(accountB.id!!)

        // Verify the cache is evicted
        verify(accountRepo, times(2)).findById(accountA.id!!)
        verify(accountRepo, times(2)).findById(accountB.id!!)
    }
}
