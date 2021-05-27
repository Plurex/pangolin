package io.plurex.pangolin.actors.statewrapper

import io.plurex.pangolin.actors.CoroutineActorStateStack
import io.plurex.pangolin.actors.ScopeParent
import io.plurex.pangolin.actors.enter
import io.plurex.pangolin.actors.exit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext


interface ActorStateWrapperI<STATE> {

    suspend fun <RESULT> execute(action: suspend STATE.() -> RESULT): RESULT

    fun stopActor()
}

open class ActorStateWrapper<STATE>(
    private val state: STATE,
    channelBuffer: Int = 10
) :
    ActorStateWrapperI<STATE> {
    private val scope: CoroutineScope = ScopeParent()
    private val myID: Int = System.identityHashCode(this)
    private val actor = scope.stateActor(
        state = state,
        wrapperID = myID,
        channelBuffer = channelBuffer
    )


    override suspend fun <RESULT> execute(action: suspend STATE.() -> RESULT): RESULT {
        if (actor.isClosedForSend) {
            throw ActorStateStoppedException()
        }
        return if (isNested()) {
            enter()
            val result = state.action()
            exit()
            result
        } else {
            val result = CompletableDeferred<RESULT>()
            actor.send(Execute(result, action))
            result.await()
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
        val stack = coroutineContext[CoroutineActorStateStack.Key] ?: throw Exception("Should always be a stack if this is nested")
        val currentDepth = stack.stateWrapperStackHeightsByID[myID] ?: throw Exception("Should always be a height if this is nested")
        stack.stateWrapperStackHeightsByID[myID] = currentDepth + 1
    }

    private suspend fun exit() {
        val stack = coroutineContext[CoroutineActorStateStack.Key] ?: throw Exception("Should always be a stack if this is nested")
        val currentDepth = stack.stateWrapperStackHeightsByID[myID] ?: throw Exception("Should always be a height if this is nested")
        stack.stateWrapperStackHeightsByID[myID] = currentDepth - 1
    }


}

class ActorStateStoppedException : Exception()

private class Execute<RESULT, STATE>(val result: CompletableDeferred<RESULT>, val action: suspend STATE.() -> RESULT)


private fun <STATE> CoroutineScope.stateActor(state: STATE, wrapperID: Int, channelBuffer: Int) =
    actor<Execute<*, STATE>>(capacity = channelBuffer) {
        for (message in channel) {
            try {
                @Suppress("UNCHECKED_CAST")
                val msgCast = message as Execute<Any?, STATE>
                val entered = enter(wrapperID)
                val result = withContext(entered) {
                    msgCast.action.invoke(state)
                }
                exit(entered, wrapperID)
                msgCast.result.complete(result)
            } catch (e: Exception) {
                message.result.completeExceptionally(e)
            }
        }
    }


