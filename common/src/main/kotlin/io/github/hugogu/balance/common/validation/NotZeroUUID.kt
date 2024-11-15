package io.github.hugogu.balance.common.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [NotZeroUUIDValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class NotZeroUUID(
    val message: String = "UUID cannot be zero",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
