package com.campoe.android.repository

import io.reactivex.Maybe
import io.reactivex.Single
import java.util.concurrent.TimeUnit

@Deprecated("Use Guava cache instead")
class InMemoryCache<K : Any, V : Any>(
    private val memoryPolicy: MemoryPolicy = MemoryPolicy()
) {

    private val memory: MutableMap<K, Entry<V>> = mutableMapOf()

    val size: Int
        get() = memory.size

    inner class Entry<V>(internal val value: V) {

        private var valid: Boolean = true
        private var accessTime: Long = Long.MAX_VALUE

        fun isValid(): Boolean {
            return valid
        }

        fun validate(): Boolean {
            if (!valid) {
                return false
            }
            if (memoryPolicy.isPersistent()) {
                return true
            }
            valid =
                System.currentTimeMillis() - accessTime < memoryPolicy.expirationTime(TimeUnit.MILLISECONDS)
            return valid
        }

        fun invalidate() {
            valid = false
        }

    }

    fun invalidate(key: K): Single<Boolean> {
        if (memory.containsKey(key)) {
            memory[key]!!.invalidate()
            return Single.just(true)
        }
        return Single.just(false)
    }

    fun invalidateAll() {
        memory.forEach {
            it.value.invalidate()
        }
    }

    fun get(key: K): Maybe<V> {
        if (memory.containsKey(key)) {
            val entry = memory[key]!!
            if (entry.validate()) {

                return Maybe.just(entry.value)
            }
        }
        return Maybe.empty()
    }

    fun getOrPut(key: K, value: V): Maybe<V> {
        if (memory.containsKey(key)) {
            val entry = memory[key]!!
            if (entry.validate()) {
                return Maybe.just(entry.value)
            }
        }
        memory[key] = Entry(value)
        return Maybe.just(value)
    }

    fun put(key: K, value: V): Single<Boolean> {
        if (memory.containsKey(key) && memory[key]!!.validate()) {
            return Single.just(false)
        }
        memory[key] = Entry(value)
        return Single.just(true)
    }

    fun remove(key: K): Maybe<V> {
        val data = memory.remove(key)
        if (data != null) {
            return Maybe.just(data.value)
        }
        return Maybe.empty()
    }

    fun clear() {
        memory.clear()
    }

    fun asMap(): Map<K, V> {
        return memory.mapValues {
            it.value.value
        }
    }

    fun cleanUp() {
        memory.values.retainAll {
            it.isValid()
        }
    }

}