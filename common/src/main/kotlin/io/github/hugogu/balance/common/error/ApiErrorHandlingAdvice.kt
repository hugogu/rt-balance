package io.github.hugogu.balance.common.error

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.http.HttpStatus
import org.springframework.util.StringUtils
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiErrorHandlingAdvice {
    @ExceptionHandler(CannotAcquireLockException::class)
    fun handException(e: CannotAcquireLockException): ErrorResponse {
        log.warn("ExceptionHandler hands CannotAcquireLockException", e)
        return ErrorResponse.create(e, HttpStatus.TOO_MANY_REQUESTS, e.message.orEmpty())
    }

    @ExceptionHandler(ConcurrentModificationException::class)
    fun handConcurrentNotificationException(e: ConcurrentModificationException): ErrorResponse {
        log.warn("ExceptionHandler hands ConcurrentModificationException", e)
        return ErrorResponse.create(e, HttpStatus.TOO_MANY_REQUESTS, e.message.orEmpty())
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handEntityNotFoundException(e: EntityNotFoundException): ErrorResponse {
        log.warn("ExceptionHandler hands EntityNotFoundException", e)
        return ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.message.orEmpty())
    }

    @ExceptionHandler(ValidationException::class)
    fun handValidationException(e: ValidationException): ErrorResponse {
        log.info("ExceptionHandler hands ViolationException", e)
        return ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.message.orEmpty())
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handMethodArgumentNotValidException(e: MethodArgumentNotValidException): ErrorResponse {
        log.info("ExceptionHandler hands MethodArgumentNotValidException", e)
        val message = StringUtils.collectionToDelimitedString(
            e.bindingResult.allErrors.stream().map {
                oe -> oe.defaultMessage
            }.toList(),"\r\n"
        )
        return ErrorResponse.create(e, HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handConstraintValidationException(e: ConstraintViolationException): ErrorResponse {
        log.info("ExceptionHandler hands ConstraintViolationException", e)
        val message = StringUtils.collectionToDelimitedString(
            e.constraintViolations.stream().map {
                cv -> cv.propertyPath + ":" + cv.message
            }.toList(), "\r\n"
        )
        return ErrorResponse.create(e, HttpStatus.BAD_REQUEST, message)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApiErrorHandlingAdvice::class.java)
    }
}
