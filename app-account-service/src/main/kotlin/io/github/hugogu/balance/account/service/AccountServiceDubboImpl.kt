package io.github.hugogu.balance.account.service

import io.github.hugogu.balance.account.repo.AccountEntity
import io.github.hugogu.balance.common.AccountServiceClient
import io.github.hugogu.balance.common.model.AccountCreationRequest
import io.github.hugogu.balance.common.model.AccountIdentity
import io.github.hugogu.balance.common.model.AccountUpdateRequest
import io.github.hugogu.balance.common.model.TransactionMessage
import org.apache.dubbo.config.annotation.DubboService
import org.springframework.stereotype.Service
import java.util.*

@DubboService(version = "1.0.0")
@Service
class AccountServiceDubboImpl(
    private val accountService: AccountService
) : AccountServiceClient {

    override fun createAccount(request: AccountCreationRequest, requestId: UUID): AccountIdentity {
        return AccountIdentity(accountService.createAccount(request.accountNumber, request.currency, request.balance, requestId).id)
    }

    override fun queryAccountDetail(id: UUID): io.github.hugogu.balance.common.model.AccountDetail {
        return accountService.queryAccountDetail(id).toDetail()
    }

    override fun processTransaction(transaction: TransactionMessage): io.github.hugogu.balance.common.model.AccountDetail {
        return accountService.processTransaction(transaction).toDetail()
    }

    override fun debitAccount(accountId: UUID, request: AccountUpdateRequest, requestId: UUID): io.github.hugogu.balance.common.model.AccountDetail {
        return accountService.debitAccount(accountId, request.amount, requestId).toDetail()
    }

    override fun creditAccount(accountId: UUID, request: AccountUpdateRequest, requestId: UUID): io.github.hugogu.balance.common.model.AccountDetail {
        return accountService.creditAccount(accountId, request.amount, requestId).toDetail()
    }

    fun AccountEntity.toDetail(): io.github.hugogu.balance.common.model.AccountDetail {
        return io.github.hugogu.balance.common.model.AccountDetail(
            id = this.id!!,
            accountNumber = this.accountNumber,
            currency = Currency.getInstance(accountCcy),
            balance = this.balance,
            status = this.status
        )
    }

    fun AccountPair.toDetail(): io.github.hugogu.balance.common.model.AccountDetail {
        return this.first.toDetail()
    }
}
