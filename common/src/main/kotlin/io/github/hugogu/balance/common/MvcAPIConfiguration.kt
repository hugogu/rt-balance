package io.github.hugogu.balance.common

import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import com.fasterxml.jackson.databind.Module
import org.zalando.jackson.datatype.money.MoneyModule

@Configuration
class MvcAPIConfiguration(
    private val modules: ObjectProvider<Module>
) : WebMvcConfigurationSupport() {
    @Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.failOnUnknownProperties(false)
            builder.modules(MoneyModule())
        }
    }
}
