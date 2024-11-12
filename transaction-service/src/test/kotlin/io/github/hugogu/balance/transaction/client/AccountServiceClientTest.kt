package io.github.hugogu.balance.transaction.client

import io.github.hugogu.balance.common.model.TransactionMessage
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ComponentScan("io.github.hugogu.balance.common")
@ActiveProfiles("test")
@Tag("integration")
class AccountServiceClientIntegrationTest {

    @Autowired
    private lateinit var accountServiceClient: AccountServiceClient

    @Test
    fun `test createAccount`() {
        val request = AccountCreationRequest(accountNumber = "123456", currency = "USD")
        val requestId = UUID.randomUUID()
        val accountIdentity = accountServiceClient.createAccount(request, requestId)

        // Verify the response
        assertNotNull(accountIdentity.id)
        assertEquals(requestId, accountIdentity.id)

        val accountDetail = accountServiceClient.queryAccountDetail(accountIdentity.id!!)
        assertEquals(accountIdentity.id, accountDetail.id)
        assertEquals(request.accountNumber, accountDetail.accountNumber)
    }

    @Test
    fun testProcessingTransaction() {
        val requestA = AccountCreationRequest(accountNumber = "123456", currency = "USD")
        val accountA = accountServiceClient.createAccount(requestA, UUID.randomUUID())
        val requestB = AccountCreationRequest(accountNumber = "123457", currency = "USD")
        val accountB = accountServiceClient.createAccount(requestB, UUID.randomUUID())

        val transaction = TransactionMessage(
            transactionId = UUID.randomUUID(),
            fromAccount = accountA.id!!,
            toAccount = accountB.id!!,
            amount = BigDecimal.TEN
        )
        accountServiceClient.processTransaction(transaction)
    }
}
