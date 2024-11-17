package io.github.hugogu.balance.common.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.UUID


class NotZeroUUIDValidator : ConstraintValidator<NotZeroUUID?, UUID?> {
    override fun isValid(value: UUID?, context: ConstraintValidatorContext?): Boolean {
        return value != null && value != ZERO_UUID
    }

    companion object {
        private val ZERO_UUID: UUID = UUID(0, 0)
    }
}
