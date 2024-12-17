package io.github.hugogu.balance.account.facade

import io.github.hugogu.balance.account.repo.OutboxRepo
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
class OutboxController(
    private val outboxRepo: OutboxRepo
) {
    @GetMapping("/account/outbox/{id}")
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    fun queryEvent(
        @PathVariable id: UUID
    ): Any {
        val entity = outboxRepo.findByIdOrNull(id) ?: throw IllegalArgumentException("Event $id not found")

        return entity.payload
    }
}
