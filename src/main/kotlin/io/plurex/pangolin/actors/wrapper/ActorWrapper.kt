package io.plurex.pangolin.actors.wrapper

import io.plurex.pangolin.actors.CoroutineActorStateStack
import io.plurex.pangolin.actors.ScopeParent
import io.plurex.pangolin.actors.enter
import io.plurex.pangolin.actors.exit
import io.plurex.pangolin.logging.aLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

private val logger = aLogger("ActorWrapper")

/**
 * Provides a [synchronized] block method call that uses an actors channel communications.
 *
 * This means that each [synchronized] block is executed atomically in sequence.
 */
interface ActorWrapperI {

    suspend fun <RESULT> synchronized(action: suspend () -> RESULT): RESULT
    suspend fun synchronizedUnit(action: suspend () -> Unit): Unit

    fun stopActor()
}

open class ActorWrapper(channelBuffer: Int = 10) : ActorWrapperI {
    private val scope: CoroutineScope = ScopeParent()
    private val myID: Int = System.identityHashCode(this)
    private val actor = scope.wrapperActor(wrapperID = myID, channelBuffer = channelBuffer)

    override suspend fun <RESULT> synchronized(action: suspend () -> RESULT): RESULT {
        if (actor.isClosedForSend) {
            throw ActorStoppedException()
        }
        return if (isNested()) {
            enter()
            val result = action()
            exit()
            result
        } else {
            val result = CompletableDeferred<RESULT>()
            actor.send(Synchronized.Result(result, action))
            result.await()
        }
    }

    override suspend fun synchronizedUnit(action: suspend () -> Unit) {
        if (actor.isClosedForSend) {
            throw ActorStoppedException()
        }
        return if (isNested()) {
            enter()
            action()
            exit()
        } else {
            actor.send(Synchronized.NoResult(action))
        }
    }

    override fun stopActor() {
        actor.close()
    }

    private suspend fun isNested(): Boolean {
        val stack = coroutineContext[CoroutineActorStateStack.Key] ?: return false
        val height = stack.stateWrapperStackHeightsByID.getOrDefault(myID, 0)
        return height >= 1
    }

    private suspend fun enter() {
        val stack = coroutineContext[CoroutineActorStateStack.Key]
            ?: throw Exception("Should always be a stack if this is nested")
        val currentDepth =
            stack.stateWrapperStackHeightsByID[myID] ?: throw Exception("Should always be a height if this is nested")
        stack.stateWrapperStackHeightsByID[myID] = currentDepth + 1
    }

    private suspend fun exit() {
        val stack = coroutineContext[CoroutineActorStateStack.Key]
            ?: throw Exception("Should always be a stack if this is nested")
        val currentDepth =
            stack.stateWrapperStackHeightsByID[myID] ?: throw Exception("Should always be a height if this is nested")
        stack.stateWrapperStackHeightsByID[myID] = currentDepth - 1
    }

}

class ActorStoppedException : Exception()

private sealed class Synchronized {
    data class Result<RESULT>(val result: CompletableDeferred<RESULT>, val action: suspend () -> RESULT) :
        Synchronized()

    data class NoResult(val action: suspend () -> Unit) : Synchronized()
}


private fun CoroutineScope.wrapperActor(wrapperID: Int, channelBuffer: Int) =
    actor<Synchronized>(capacity = channelBuffer) {
        for (message in channel) {
            try {
                @Suppress("UNCHECKED_CAST")
                when (message) {
                    is Synchronized.Result<*> -> {
                        val msgCast = message as Synchronized.Result<Any?>
                        val entered = enter(wrapperID)
                        val result = withContext(entered) {
                            msgCast.action.invoke()
                        }
                        exit(entered, wrapperID)
                        msgCast.result.complete(result)
                    }
                    is Synchronized.NoResult -> {
                        val entered = enter(wrapperID)
                        withContext(entered) {
                            message.action.invoke()
                        }
                        exit(entered, wrapperID)
                    }
                }
            } catch (e: Exception) {
                when (message) {
                    is Synchronized.Result<*> -> {
                        message.result.completeExceptionally(e)
                    }
                    else -> {
                        logger.error("Error in non blocking unit call", e)
                    }
                }
            }
        }
    }
