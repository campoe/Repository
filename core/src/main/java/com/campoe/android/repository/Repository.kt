package com.campoe.android.repository

import com.campoe.android.repository.persistence.Fetcher
import com.campoe.android.repository.persistence.Persister
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class Repository<K : Any, V : Any> private constructor(
    protected val fetcher: Fetcher<K, V>,
    protected val persister: Persister<K, V>,
    memoryPolicy: MemoryPolicy = MemoryPolicy()
) : IRepository<K, V> {

    private val inFlight: Cache<K, Single<V>> =
        CacheBuilder.newBuilder().reuseInFlight(memoryPolicy).build()
    private val cache: Cache<K, Maybe<V>> =
        CacheBuilder.newBuilder().memoryPolicy(memoryPolicy).build()
    private val subject: PublishSubject<Pair<K, V>> = PublishSubject.create()

    override fun get(key: K): Single<V> {
        return Maybe.defer {
                cache.getIfPresent(key) ?: Maybe.empty()
            }.onErrorResumeNext(Maybe.empty())
            .switchIfEmpty(fetch(key).toMaybe())
            .toSingle()
    }

    override fun fetch(key: K): Single<V> {
        return Single.defer {
            try {
                inFlight.get(key, { response(key) })
            } catch (e: ExecutionException) {
                Single.error(e)
            }
        }
    }

    private fun readDisk(key: K): Maybe<V> {
        return persister.get(key)
            .onErrorResumeNext(Maybe.empty())
            .doOnSuccess {
                cache.put(key, Maybe.just(it))
            }.cache()
    }

    private fun response(key: K): Single<V> {
        return fetcher.fetch(key)
            .flatMap {
                persister.put(key, it)
                    .flatMap { readDisk(key).toSingle() }
            }
            .onErrorResumeNext {
                Single.error(it)
            }
            .doOnSuccess {
                subject.onNext(key to it)
            }
            .doAfterTerminate { inFlight.invalidate(key) }
            .cache()
    }

    override fun clear() {
        inFlight.invalidateAll(cache.asMap().keys)
        cache.invalidateAll()
    }

    override fun remove(key: K) {
        inFlight.invalidate(key)
        cache.invalidate(key)
    }

    class Builder<K : Any, V : Any> {

        private var fetcher: Fetcher<K, V>? = null
        private var persister: Persister<K, V>? = null
        private var memoryPolicy: MemoryPolicy = MemoryPolicy(24, TimeUnit.HOURS, 100)

        fun fetcher(fetcher: Fetcher<K, V>): Builder<K, V> {
            this.fetcher = fetcher
            return this
        }

        fun persister(persister: Persister<K, V>): Builder<K, V> {
            this.persister = persister
            return this
        }

        fun memoryPolicy(memoryPolicy: MemoryPolicy): Builder<K, V> {
            this.memoryPolicy = memoryPolicy
            return this
        }

        fun build(): Repository<K, V> {
            if (persister == null) {
                persister = object : Persister<K, V> {
                    override fun get(key: K): Maybe<V> {
                        return Maybe.empty()
                    }

                    override fun put(key: K, value: V): Single<Boolean> {
                        return Single.just(true)
                    }
                }
            }
            if (fetcher == null) {
                throw NullPointerException()
            }
            return Repository(fetcher!!, persister!!)
        }

    }

}