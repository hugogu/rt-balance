package io.github.hugogu.balance.account.repo

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OutboxRepo : CrudRepository<OutboxEntity, UUID>
