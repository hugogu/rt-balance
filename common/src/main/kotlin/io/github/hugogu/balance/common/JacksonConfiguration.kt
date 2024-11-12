package io.github.hugogu.balance.common

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.jackson.datatype.money.MoneyModule

@Configuration
class JacksonConfiguration {
    @Bean
    fun moneyModule() = MoneyModule()

    @Bean
    fun customizedObjectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()
}
