package com.campoe.android.repository.persistence

import io.reactivex.Single

interface Fetcher<K : Any, V : Any> {

    fun fetch(key: K): Single<V>

}