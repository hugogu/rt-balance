package io.github.hugogu.balance.account.service

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentModificationException

/**
 * Redisson-backed implementation of [DistributedLockService].
 *
 * Uses [org.redisson.api.RLock.tryLock] with `leaseTime = -1` so that Redisson
 * activates its built-in **watch dog**: the lease is auto-renewed for as long
 * as the critical section is running. This eliminates the race where the
 * previous `valueOperations.setIfAbsent(key, value, lockTimeout)` would let
 * the lock expire mid-transaction if the action ran longer than `lockTimeout`,
 * causing another thread to acquire the same key and produce double-writes /
 * double-debits.
 *
 * The [RedissonClient] is auto-configured by `redisson-spring-boot-starter`
 * from the existing `spring.data.redis.host` / `spring.data.redis.port`
 * properties — no extra configuration needed.
 */
@Service
class RedissonDistributedLockService(
    private val redissonClient: RedissonClient
) : DistributedLockService {

    override fun <T> executeWithLock(lockKey: String, waitTime: Duration, action: () -> T): T {
        val lock = redissonClient.getLock(lockKey)
        val acquired = lock.tryLock(waitTime.toMillis(), LEASE_TIME_USE_WATCHDOG, TimeUnit.MILLISECONDS)
        if (!acquired) {
            throw ConcurrentModificationException("LockKey $lockKey is already being processed")
        }
        try {
            return action()
        } finally {
            // isHeldByCurrentThread guards against the unusual case where the
            // lease expired AND the watchdog was disabled (e.g. leaseTime > 0);
            // unlocking a lock we don't own would throw IllegalMonitorStateException.
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }

    companion object {
        /**
         * Sentinel value understood by [org.redisson.api.RLock.tryLock]: enables
         * the watchdog-based auto-renewal with the default
         * `lockWatchdogTimeout` (30s). Renewal cadence is watchdogTimeout / 3,
         * so a 30s lease is renewed every 10s.
         */
        private const val LEASE_TIME_USE_WATCHDOG = -1L
    }
}