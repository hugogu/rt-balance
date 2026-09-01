package io.github.hugogu.balance.account.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentModificationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test that exercises the real Redisson-backed
 * [RedissonDistributedLockService] against a live Redis. Skipped unless the
 * `spring.data.redis.host` / `spring.data.redis.port` are reachable — the
 * project CI already runs a `redis:7` service container alongside
 * `postgres:17`, so this test passes there without any extra wiring.
 *
 * Tagged `integration` so `./gradlew test -DexcludeTags=integration` keeps
 * skipping it locally when Redis is unavailable.
 */
@Tag("integration")
@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestPropertySource(properties = [
    "service.lock.timeout=10s",
    "spring.kafka.bootstrap-servers=PLAINTEXT://localhost:0"
])
class RedissonDistributedLockServiceIntegrationTest {
    @Autowired
    private lateinit var lockService: RedissonDistributedLockService

    private var localClient: RedissonClient? = null
    private val lockKeys = mutableListOf<String>()

    @AfterEach
    fun tearDown() {
        localClient?.let { client ->
            lockKeys.forEach { runCatching { client.getLock(it).delete() } }
            runCatching { client.shutdown() }
        }
    }

    @Test
    fun acquiredLockRunsActionAndReleases() {
        val key = newKey()
        val counter = AtomicInteger(0)

        val result = lockService.executeWithLock(key, Duration.ofSeconds(1)) {
            counter.incrementAndGet()
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(1, counter.get())

        // Lock should be free for the next caller.
        lockService.executeWithLock(key, Duration.ZERO) {
            assertEquals(2, counter.incrementAndGet())
        }
        assertEquals(2, counter.get())
    }

    @Test
    fun contendedLockFailsFast() {
        val key = newKey()
        val holderStarted = CountDownLatch(1)
        val holderRelease = CountDownLatch(1)
        val holderDone = CountDownLatch(1)

        val holder = Executors.newSingleThreadExecutor()
        try {
            holder.submit {
                lockService.executeWithLock(key, Duration.ZERO) {
                    holderStarted.countDown()
                    holderRelease.await(5, TimeUnit.SECONDS)
                }
                holderDone.countDown()
            }
            assertTrue(holderStarted.await(5, TimeUnit.SECONDS), "holder thread did not start")

            // Second caller asks for ZERO wait and must immediately surface
            // ConcurrentModificationException — same contract the previous
            // SET-NX implementation gave callers.
            assertFailsWith<ConcurrentModificationException> {
                lockService.executeWithLock(key, Duration.ZERO) { error("inner must not run") }
            }
        } finally {
            holderRelease.countDown()
            holder.shutdown()
            holderDone.await(5, TimeUnit.SECONDS)
        }
    }

    /**
     * Stands up a private Redisson client with a 2s watchdog and verifies
     * the lease is auto-renewed while the holder sleeps past two renewals
     * (3.5s total). This proves the race in the previous SET-NX implementation
     * — lock expiring mid-action — is gone.
     */
    @Test
    fun watchdogKeepsLockAlivePastLease() {
        val cfg = Config().apply {
            useSingleServer().apply {
                address = System.getenv("REDIS_URL") ?: "redis://127.0.0.1:6379"
                connectionPoolSize = 4
                connectionMinimumIdleSize = 2
            }
            // Lower watchdog so we can observe renewal cadence quickly.
            lockWatchdogTimeout = 2_000
        }
        val client = Redisson.create(cfg)
        localClient = client
        val local = RedissonDistributedLockService(client)
        val key = newKey()

        val lock = client.getLock(key)
        val firstAcquired = lock.tryLock(0, -1, TimeUnit.MILLISECONDS)
        assertTrue(firstAcquired, "first acquire should succeed")

        var contendedFails = false
        var secondAcquired = false
        try {
            // Hold the lock past 1.75 × watchdog (2/3s renewal cadence × ~3 renewals).
            Thread.sleep(3_500)
            assertTrue(lock.isHeldByCurrentThread, "watchdog should keep lock alive")
            assertTrue(lock.remainTimeToLive() > 0, "lock TTL should still be positive after 3.5s sleep")

            // A second caller with zero wait must still fail — lock is legitimately held.
            contendedFails = runCatching {
                local.executeWithLock(key, Duration.ZERO) { error("should not run") }
            }.exceptionOrNull() is ConcurrentModificationException
        } finally {
            if (lock.isHeldByCurrentThread) lock.unlock()
        }

        // After we release, a new caller succeeds.
        secondAcquired = runCatching {
            local.executeWithLock(key, Duration.ofSeconds(1)) { true }
        }.getOrDefault(false)

        assertNotNull(client)
        assertTrue(contendedFails, "second caller should fail fast while lock is held")
        assertTrue(secondAcquired, "second caller should succeed after release")
    }

    private fun newKey(): String = "lock-it:" + UUID.randomUUID().also { lockKeys.add(it) }
}