package io.github.hugogu.balance.transaction.client

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.UUID

class AccountServiceClientTest {
    private lateinit var accountServiceClient: AccountServiceClient

    @BeforeEach
    fun setUp() {
        accountServiceClient = mock()
        whenever(accountServiceClient.creditAccountAmount(any(), any(), any())).thenCallRealMethod()
        whenever(accountServiceClient.debitAccountAmount(any(), any(), any())).thenCallRealMethod()
    }

    @Test
    fun debitAccountTest() {
        val accountId = UUID.randomUUID()
        val transactionId = UUID.randomUUID()
        accountServiceClient.debitAccountAmount(accountId, BigDecimal.ONE, transactionId)

        argumentCaptor<AccountUpdateRequest> {
            verify(accountServiceClient, times(1)).debitAccount(eq(accountId), capture(), any())

            assertThat(firstValue.amount, equalTo(BigDecimal.ONE))
        }
    }

    @Test
    fun creditAccountTest() {
        val accountId = UUID.randomUUID()
        val transactionId = UUID.randomUUID()
        accountServiceClient.creditAccountAmount(accountId, BigDecimal.ONE, transactionId)

        argumentCaptor<AccountUpdateRequest> {
            verify(accountServiceClient, times(1)).creditAccount(eq(accountId), capture(), any())

            assertThat(firstValue.amount, equalTo(BigDecimal.ONE))
        }
    }
}
