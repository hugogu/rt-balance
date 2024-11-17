package io.github.hugogu.balance.transaction.repo

/**
 * This is a mix of business status and technical status. Works but not good.
 *
 * TODO: This should be split into two different enums. Even better,
 *       need to introduce a new layer to handle the tech status.
 */
enum class TransactionStatus {
    PENDING,
    COMPLETED,
    /**
     * A transaction is marked as failed when it is not able to be settled, maybe for legal reasons.
     */
    FAILED,
}
