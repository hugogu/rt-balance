package io.github.hugogu.balance.account.facade

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.hugogu.balance.common.model.TransactionMessage
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

@Tag("integration")
@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun createAccountAndQueryDetailTest() {
        val requestId = UUID.randomUUID()
        val accountCreationRequest = AccountCreationRequest(
            accountNumber = "123456",
            balance = BigDecimal("10000")
        )
        mockMvc.perform(
            post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", requestId.toString())
                .content(objectMapper.writeValueAsString(accountCreationRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(requestId.toString()))

        mockMvc.perform(get("/account/$requestId").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(requestId.toString()))
            .andExpect(jsonPath("$.accountNumber").value("123456"))
            .andExpect(jsonPath("$.accountCurrency").value("USD"))
            .andExpect(jsonPath("$.balance").value(10000.0))
    }

    @ParameterizedTest
    @CsvSource("{}", "{")
    fun invalidRequestBodyShouldFail(body: String) {
        mockMvc.perform(
            post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest)
    }

    @ParameterizedTest
    // These are not valid account ids
    @CsvSource(
        "c37cf9a4-7f51-4dc3-b3e5-8e7d334c092a",
        "00000000-0000-0000-0000-000000000000"
    )
    fun transactionWithWrongAccountShouldFail(accountId: UUID) {
        val transactionMessage = TransactionMessage(
            transactionId = UUID.randomUUID(),
            fromAccount = accountId,
            toAccount = accountId,
            amount = BigDecimal("100.00")
        )

        mockMvc.perform(
            post("/account:transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionMessage)))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun accountDebitCreditTest() {
        val accountA = UUID.randomUUID()
        val accountACreationRequest = AccountCreationRequest(
            accountNumber = "123456",
            balance = BigDecimal("10000")
        )
        mockMvc.perform(
            post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", accountA.toString())
                .content(objectMapper.writeValueAsString(accountACreationRequest)))

        mockMvc.perform(
            post("/account:debit/$accountA")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", accountA.toString())
                .content(objectMapper.writeValueAsString(AccountDebitRequest(BigDecimal("10000")))))

        mockMvc.perform(
            post("/account:credit/$accountA")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", accountA.toString())
                .content(objectMapper.writeValueAsString(AccountDebitRequest(BigDecimal("3000")))))

        mockMvc.perform(get("/account/$accountA").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(accountA.toString()))
            .andExpect(jsonPath("$.accountNumber").value("123456"))
            .andExpect(jsonPath("$.accountCurrency").value("USD"))
            .andExpect(jsonPath("$.balance").value(3000))
    }

    @Test
    fun processTransactionTest() {
        val accountA = UUID.randomUUID()
        val accountACreationRequest = AccountCreationRequest(
            accountNumber = "123456",
            balance = BigDecimal("10000")
        )
        mockMvc.perform(
            post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", accountA.toString())
                .content(objectMapper.writeValueAsString(accountACreationRequest)))
        val accountB = UUID.randomUUID()
        val accountBCreationRequest = AccountCreationRequest(
            accountNumber = "123457",
            balance = BigDecimal("10000")
        )
        mockMvc.perform(
            post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", accountB.toString())
                .content(objectMapper.writeValueAsString(accountBCreationRequest)))
        val transactionMessage = TransactionMessage(
            transactionId = UUID.randomUUID(),
            fromAccount = accountA,
            toAccount = accountB,
            amount = BigDecimal("100.00")
        )

        mockMvc.perform(
            post("/account:transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", transactionMessage.transactionId.toString())
                .content(objectMapper.writeValueAsString(transactionMessage)))
            .andExpect(status().isOk)
    }
}
