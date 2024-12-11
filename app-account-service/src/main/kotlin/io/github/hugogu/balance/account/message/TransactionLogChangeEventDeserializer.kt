package io.github.hugogu.balance.account.message

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.kafka.support.serializer.JsonDeserializer

class TransactionLogChangeEventDeserializer : JsonDeserializer<TransactionLogChangeEvent>(
    object : TypeReference<TransactionLogChangeEvent>() {}
)
