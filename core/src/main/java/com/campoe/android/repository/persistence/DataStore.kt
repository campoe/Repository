package com.campoe.android.repository.persistence

import io.reactivex.Maybe
import io.reactivex.Single
import java.util.concurrent.locks.ReentrantLock

abstract class DataStore<K : Any, V : Any> : IDataStore<K, V>, ReentrantLock() {

    override fun getOrPut(key: K, loader: () -> V): Maybe<V> {
        return getOrPut(key, loader())
    }

    override fun get(key: K): Maybe<V> {
        throw NotImplementedError()
    }

    final override fun fetch(key: K): Single<V> {
        return get(key).toSingle()
    }

    override fun getOrPut(key: K, value: V): Maybe<V> {
        throw NotImplementedError()
    }

    override fun put(key: K, value: V): Single<Boolean> {
        throw NotImplementedError()
    }

    override fun remove(key: K): Maybe<V> {
        throw NotImplementedError()
    }

}