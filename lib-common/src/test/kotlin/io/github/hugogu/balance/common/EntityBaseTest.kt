package io.github.hugogu.balance.common

import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class EntityBaseTest {
    @Test
    fun defaultValueTest() {
        val entityBase = object : EntityBase() { }
        assertNull(entityBase.id)
        assertFalse(entityBase.isNew)
        assertEquals(Instant.EPOCH, entityBase.createTime)

        entityBase.setId(UUID(0, 0))
        // Should still be new with an ID field.
        // to allow insert value with custom id.
        assertFalse(entityBase.isNew)
    }
}
