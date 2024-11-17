package io.github.hugogu.balance.transaction.facade

import io.github.hugogu.balance.transaction.service.error.TransactionBusinessError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class TransactionControllerAdvice {
    @ExceptionHandler(TransactionBusinessError::class)
    fun handTransactionBusinessError(e: TransactionBusinessError): ErrorResponse {
        log.warn("ExceptionHandler hands TransactionBusinessError", e)
        // For known business errors, we return 400 to distinguish from normal logic.
        return ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.message.orEmpty())
    }

    companion object {
        private val log = LoggerFactory.getLogger(TransactionControllerAdvice::class.java)
    }
}
