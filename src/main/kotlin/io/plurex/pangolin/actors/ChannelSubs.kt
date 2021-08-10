package io.plurex.pangolin.actors

import io.plurex.pangolin.actors.wrapper.ActorWrapper
import io.plurex.pangolin.logging.HasLogger
import io.plurex.pangolin.logging.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel


class ChannelSubs<MESSAGE_TYPE>(
    private val name: String,
    private val capacity: Int = 10,
    private val scope: CoroutineScope = ScopeParent(),
    private val sendTimeoutMillis: Long = 1000,
    private val debug: Boolean = false
) :
    ActorWrapper(channelBuffer = 0),
    HasLogger {

    private val subscriptions = mutableListOf<Channel<MESSAGE_TYPE>>()

    suspend fun newSub(): Channel<MESSAGE_TYPE> = synchronized {
        val channel = Channel<MESSAGE_TYPE>(capacity = capacity)
        subscriptions.add(channel)
        channel
    }

    suspend fun send(message: MESSAGE_TYPE) = synchronized {
        logDebug("Sending $message to ${subscriptions.size} subs")
        val closedChannels = mutableListOf<Channel<MESSAGE_TYPE>>()
        val sendingJobs = mutableListOf<Job>()
        subscriptions.forEach { subChannel ->
            if (subChannel.isClosedForSend) {
                closedChannels.add(subChannel)
            } else {
                sendingJobs.add(
                    scope.launch {
                        try {
                            withTimeout(sendTimeoutMillis) {
                                subChannel.send(message)
                            }
                        } catch (exception: Throwable) {
                            if (exception !is CancellationException) {
                                logger().warn("$name: Sending to subscription exception.", exception)
                            }
                            try {
                                subChannel.cancel()
                            } catch (e: Exception) {
                                // Do nothing - best effort has been made
                            }
                            closedChannels.add(subChannel)
                        }
                    }
                )
            }
        }
        logDebug("Waiting for senders to join")
        sendingJobs.joinAll()
        logDebug("Senders have joined")
        subscriptions.removeAll(closedChannels)
    }

    suspend fun closeAllSubs() = synchronized {
        subscriptions.forEach { it.close() }
    }

    private fun logDebug(message: String) {
        if (debug) {
            logger().debug("$name: $message")
        }
    }
}