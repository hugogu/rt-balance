package io.github.hugogu.balance.common.validation

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse

class NotZeroUUIDValidatorTest {
    @Test
    fun invalidUUIDTest() {
        val notZeroUUIDValidator = NotZeroUUIDValidator()
        assertFalse(notZeroUUIDValidator.isValid(UUID(0, 0), null))
        assertFalse(notZeroUUIDValidator.isValid(null, null))
    }
}
