package io.plurex.common.actors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch


fun <T> CoroutineScope.mergeChannels(sourceChannels: List<ReceiveChannel<T>>, capacity: Int = 0): ReceiveChannel<T> {
    return produce(capacity = capacity) {
        sourceChannels.forEach { sourceChannel ->
            launch { sourceChannel.consumeEach { send(it) } }
        }
    }
}