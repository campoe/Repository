package com.campoe.android.repository.persistence

import io.reactivex.Maybe

interface Readable<K, V> {

    fun get(key: K): Maybe<V>

}