package io.github.hugogu.balance.common.error
import io.github.hugogu.balance.common.model.TransactionMessage
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ValidationException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.dao.CannotAcquireLockException
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class ApiErrorHandlingAdviceTest {
    private val advice = ApiErrorHandlingAdvice()

    @Test
    fun cannotAcquireLockExceptionHandlerTest() {
        val exception = CannotAcquireLockException("Lock not acquired")
        val response = advice.handException(exception)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.statusCode)
        assertThat(response.body.detail, containsString(exception.message))
    }

    @Test
    fun concurrentModificationExceptionHandlerTest() {
        val exception = ConcurrentModificationException("Concurrent modification")
        val response = advice.handConcurrentNotificationException(exception)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.statusCode)
        assertThat(response.body.detail, containsString(exception.message))
    }

    @Test
    fun entityNotFoundExceptionHandlerTest() {
        val exception = EntityNotFoundException("Entity not found")
        val response = advice.handEntityNotFoundException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertThat(response.body.detail, containsString(exception.message))
    }

    @Test
    fun validationExceptionHandlerTest() {
        val exception = ValidationException("Validation failed")
        val response = advice.handValidationException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertThat(response.body.detail, containsString(exception.message))
    }

    @Test
    fun methodArgumentNotValidExceptionHandlerTest() {
        val parameter = mock(MethodParameter::class.java)
        val bindingResult = mock(BindingResult::class.java)
        val error = FieldError("Account", "id", "Not Empty.")
        whenever(bindingResult.allErrors).thenReturn(listOf(error))
        whenever(parameter.executable).thenReturn(TransactionMessage::class.java.getMethod("toString"))
        val exception = MethodArgumentNotValidException(parameter, bindingResult)
        val response = advice.handMethodArgumentNotValidException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertThat(response.body.detail, containsString(error.defaultMessage))
    }

    @Test
    fun constraintViolationExceptionHandlerTest() {
        val exception = ConstraintViolationException("Constraint violation", emptySet())
        val response = advice.handConstraintValidationException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}
