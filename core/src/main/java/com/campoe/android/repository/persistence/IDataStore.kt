package com.campoe.android.repository.persistence

import io.reactivex.Maybe
import io.reactivex.Single

internal interface IDataStore<K : Any, V : Any> : Fetcher<K, V>, Persister<K, V> {

    override fun get(key: K): Maybe<V>
    fun getOrPut(key: K, value: V): Maybe<V>
    fun getOrPut(key: K, loader: () -> V): Maybe<V>
    override fun put(key: K, value: V): Single<Boolean>
    fun remove(key: K): Maybe<V>

}