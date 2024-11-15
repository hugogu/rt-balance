package io.github.hugogu.balance.common

import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport

@Configuration
class MvcAPIConfiguration : WebMvcConfigurationSupport() {
    @Bean
    fun jackson2ObjectMapperBuilderCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        it.failOnUnknownProperties(false)
    }
}
