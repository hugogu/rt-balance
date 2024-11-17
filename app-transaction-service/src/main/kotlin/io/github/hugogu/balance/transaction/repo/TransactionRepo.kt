package io.github.hugogu.balance.transaction.repo

import jakarta.persistence.EntityNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface TransactionRepo : JpaRepository<TransactionEntity, UUID> {
    @Transactional
    fun updateTransactionStatus(transactionId: UUID, status: TransactionStatus): TransactionEntity {
        val transaction = findById(transactionId).orElseThrow {
            EntityNotFoundException("Cannot find transaction $transactionId")
        }
        transaction.status = status
        return save(transaction)
    }
}
