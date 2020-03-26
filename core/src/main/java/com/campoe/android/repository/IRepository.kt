package com.campoe.android.repository

import io.reactivex.Single

interface IRepository<K : Any, V : Any> {

    fun get(key: K): Single<V>

    fun fetch(key: K): Single<V>

    fun clear()

    fun remove(key: K)

}