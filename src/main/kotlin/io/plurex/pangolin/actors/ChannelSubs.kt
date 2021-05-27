package io.plurex.pangolin.actors

import io.plurex.pangolin.logging.HasLogger
import io.plurex.pangolin.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch


class ChannelSubs<MESSAGE_TYPE>(private val capacity: Int = 10, private val scope: CoroutineScope = ScopeParent()) : HasLogger {

    private val subscriptions = mutableListOf<Channel<MESSAGE_TYPE>>()

    fun newSub(): Channel<MESSAGE_TYPE> {
        val channel = Channel<MESSAGE_TYPE>(capacity = capacity)
        subscriptions.add(channel)
        return channel
    }


    suspend fun send(message: MESSAGE_TYPE) {
        val closedChannels = mutableListOf<Channel<MESSAGE_TYPE>>()
        val sendingJobs = mutableListOf<Job>()
        subscriptions.forEach { subChannel ->
            if (subChannel.isClosedForSend) {
                closedChannels.add(subChannel)
            } else {
                sendingJobs.add(scope.launch {

                    try {
                        subChannel.send(message)
                    } catch (exception: Throwable) {
                        logger().warn("Sending to subscription exception.", exception)
                        closedChannels.add(subChannel)
                    }
                })
            }
        }
        subscriptions.removeAll(closedChannels)
        sendingJobs.joinAll()
    }

    fun closeAllSubs() {
        subscriptions.forEach { it.close() }
    }

}