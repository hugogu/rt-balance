package io.github.hugogu.balance.transaction.facade

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.hugogu.balance.common.model.AccountStatus
import io.github.hugogu.balance.common.model.TransactionMessage
import io.github.hugogu.balance.transaction.client.AccountDetail
import io.github.hugogu.balance.transaction.client.AccountServiceClient
import io.github.hugogu.balance.transaction.repo.TransactionRepo
import io.github.hugogu.balance.transaction.repo.TransactionStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.sql.SQLException
import java.util.UUID
import kotlin.test.assertEquals

@Tag("integration")
@SpringBootTest
@Import(TestConfiguration::class)
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @MockBean
    private lateinit var accountServiceClient: AccountServiceClient
    @SpyBean
    private lateinit var transactionRepo: TransactionRepo

    @BeforeEach
    fun setup() {
        whenever(accountServiceClient.processTransactionWithRetry(any())).thenCallRealMethod()
    }

    @Test
    fun createNewTransactionTest() {
        val requestId = UUID.randomUUID()
        val (fromAccount, toAccount) = mockAccountPair(AccountStatus.ACTIVE)
        val newTransaction = TransferRequest(
            from = fromAccount.id,
            to = toAccount.id,
            amount = BigDecimal("100")
        )
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .content(objectMapper.writeValueAsString(newTransaction)))
            .andExpect(status().isCreated)

        argumentCaptor<TransactionMessage> {
            verify(accountServiceClient, times(1)).processTransaction(capture())
            assertEquals(requestId, firstValue.transactionId)
            assertEquals(fromAccount.id, firstValue.fromAccount)
            assertEquals(toAccount.id, firstValue.toAccount)
            assertEquals(0, newTransaction.amount.compareTo(firstValue.amount))
        }
    }

    @Test
    fun createNewTransactionOnInactiveAccountTest() {
        val requestId = UUID.randomUUID()
        val (fromAccount, toAccount) = mockAccountPair(AccountStatus.PENDING)
        val newTransaction = TransferRequest(
            from = fromAccount.id,
            to = toAccount.id,
            amount = BigDecimal("100")
        )
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .content(objectMapper.writeValueAsString(newTransaction)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.detail").value("Account status is not active"))
    }

    @Test
    fun createNewTransactionWithCompletionErrorTest() {
        val requestId = UUID.randomUUID()
        val (fromAccount, toAccount) = mockAccountPair(AccountStatus.ACTIVE)
        val newTransaction = TransferRequest(
            from = fromAccount.id,
            to = toAccount.id,
            amount = BigDecimal("100")
        )
        whenever(accountServiceClient.processTransactionWithRetry(any()))
            .thenThrow(BadSqlGrammarException("update", "sql", SQLException()))

        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .content(objectMapper.writeValueAsString(newTransaction)))
            .andExpect(status().isCreated)

        transactionRepo.findById(requestId).get().let {
            assertEquals(TransactionStatus.FAILED, it.status)
        }
    }

    private fun mockAccountPair(status: AccountStatus): Pair<AccountDetail, AccountDetail> {
        val fromAccount = UUID.randomUUID()
        val toAccount = UUID.randomUUID()
        val fromAccountDetail = AccountDetail(
            id = fromAccount,
            status = status,
            accountNumber = "123456",
            balance = BigDecimal("1000")
        )
        val toAccountDetail = AccountDetail(
            id = toAccount,
            status = status,
            accountNumber = "123457",
            balance = BigDecimal("1000")
        )
        whenever(accountServiceClient.queryAccountDetail(eq(fromAccount))).thenReturn(fromAccountDetail)
        whenever(accountServiceClient.queryAccountDetail(eq(toAccount))).thenReturn(toAccountDetail)

        return fromAccountDetail to toAccountDetail
    }
}
