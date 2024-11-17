package io.github.hugogu.balance.account.config

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfig {
    @Bean
    fun cacheConfiguration(): RedisCacheConfiguration {
        val serializer = GenericJackson2JsonRedisSerializer(null).configure {
            it.findAndRegisterModules()
        }
        return RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .serializeKeysWith(
                SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                SerializationPair.fromSerializer(serializer)
            )
    }

    @Bean
    fun redisCacheManagerBuilderCustomizer(cacheConfig: RedisCacheConfiguration): RedisCacheManagerBuilderCustomizer {
        return RedisCacheManagerBuilderCustomizer {
            it.withCacheConfiguration(
                ACCOUNT_DETAIL_CACHE,
                cacheConfig.entryTtl(CACHE_TTL)
            )
        }
    }

    companion object {
        const val ACCOUNT_DETAIL_CACHE = "accountDetails"
        private val CACHE_TTL = Duration.ofMinutes(60)
    }
}
