package io.github.hugogu.balance.account.repo

/**
 * Represents the processing status of a transaction.
 * It is only used in async processing, in sync processing, the status is always PROCESSED.
 */
enum class ProcessingStatus {
    INIT,
    SUCCEED,
    FAILED,
}
