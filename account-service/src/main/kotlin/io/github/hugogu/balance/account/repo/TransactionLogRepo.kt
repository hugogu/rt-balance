package io.github.hugogu.balance.account.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TransactionLogRepo : JpaRepository<TransactionLogEntity, UUID> {
}
