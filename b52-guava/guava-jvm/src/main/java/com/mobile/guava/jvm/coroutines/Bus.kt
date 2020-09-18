package com.mobile.guava.jvm.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

object Bus {

    @ExperimentalCoroutinesApi
    private val channel = BroadcastChannel<Pair<Int, Any>>(1)

    @ExperimentalCoroutinesApi
    fun offer(id: Int, data: Any) = channel.offer(Pair(id, data))

    @ExperimentalCoroutinesApi
    fun offer(id: Int) = channel.offer(Pair(id, Unit))

    @ExperimentalCoroutinesApi
    fun subscribe(): Flow<Pair<Int, Any>> = channel.openSubscription().consumeAsFlow()
}