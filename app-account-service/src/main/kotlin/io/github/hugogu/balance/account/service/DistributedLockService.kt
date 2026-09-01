package io.github.hugogu.balance.account.service

import java.time.Duration

/**
 * Abstraction over a distributed lock provider so business code doesn't depend
 * directly on a specific client (Redisson, Lettuce+Lua, Zookeeper, etc.) and
 * can be unit-tested with a simple mock.
 */
interface DistributedLockService {
    /**
     * Run [action] while holding a distributed lock identified by [lockKey].
     *
     * Semantics:
     * - If the lock is acquired, [action] runs and its result is returned.
     * - If the lock is **not** acquired within [waitTime], [action] is **not**
     *   invoked and a [ConcurrentModificationException] is thrown — same as the
     *   previous Redis SET-NX implementation, so callers and the HTTP error
     *   mapping (429) keep working.
     * - On any exception from [action], the lock is released before rethrowing.
     * - The lease is automatically renewed (watch dog) so that long-running
     *   actions cannot lose the lock mid-transaction — the previous
     *   `SET NX EX <lockTimeout>` semantics had this race condition and are
     *   fixed here.
     *
     * @param lockKey unique key for the lock (caller is responsible for naming).
     * @param waitTime how long to wait for the lock before failing. Pass
     *   [Duration.ZERO] to fail immediately.
     * @param action the critical section.
     */
    fun <T> executeWithLock(lockKey: String, waitTime: Duration, action: () -> T): T
}