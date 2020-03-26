package com.campoe.android.repository.persistence

import io.reactivex.Maybe
import io.reactivex.Single

interface Persister<K : Any, V : Any> {

    fun get(key: K): Maybe<V>

    fun put(key: K, value: V): Single<Boolean>

}