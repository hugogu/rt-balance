package io.github.hugogu.balance.common.model

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

class TransactionMessageTest {
    @Test
    fun defaultValueZeroTest() {
        val transactionMessage = TransactionMessage()
        assertEquals(UUID(0, 0), transactionMessage.transactionId)
        assertEquals(UUID(0, 0), transactionMessage.fromAccount)
        assertEquals(UUID(0, 0), transactionMessage.toAccount)
        assertEquals(BigDecimal.ZERO, transactionMessage.amount)
        assertEquals(Instant.EPOCH, transactionMessage.timestamp)
    }

    @Test
    fun zeroValueEqualityTest() {
        assertEquals(TransactionMessage(), TransactionMessage())
    }
}
