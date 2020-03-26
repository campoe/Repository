package com.campoe.android.repository

import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

data class MemoryPolicy(
    val expirationTime: Long = -1,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    val maximumSize: Long = 100
) {

    fun isPersistent(): Boolean = expirationTime < 0

    fun expirationTime(timeUnit: TimeUnit): Long =
        timeUnit.convert(this.expirationTime, this.timeUnit)

}

internal fun <K : Any, V : Any> CacheBuilder<K, V>.reuseInFlight(memoryPolicy: MemoryPolicy): CacheBuilder<K, V> {
    val inFlightExpirationTime = 60L
    if (memoryPolicy.expirationTime(TimeUnit.SECONDS) > inFlightExpirationTime) {
        return expireAfterWrite(inFlightExpirationTime, TimeUnit.SECONDS)
    } else {
        return expireAfterWrite(memoryPolicy.expirationTime, memoryPolicy.timeUnit)
    }
}

internal fun <K : Any, V : Any> CacheBuilder<K, V>.memoryPolicy(memoryPolicy: MemoryPolicy): CacheBuilder<K, V> {
    return maximumSize(memoryPolicy.maximumSize).expireAfterAccess(
        memoryPolicy.expirationTime,
        memoryPolicy.timeUnit
    )
}