package io.plurex.pangolin.actors

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

internal suspend fun enter(wrapperID: Int): CoroutineContext {
    var context = coroutineContext
    var stack = context[CoroutineActorStateStack.Key]
    if (stack == null) {
        stack = CoroutineActorStateStack()
        context = context.plus(stack)
    }
    val currentDepth = stack.stateWrapperStackHeightsByID.getOrPut(wrapperID) { 0 }
    stack.stateWrapperStackHeightsByID[wrapperID] = currentDepth + 1
    return context
}

internal fun exit(context: CoroutineContext, wrapperID: Int) {
    val stack = context[CoroutineActorStateStack.Key] ?: throw Exception("This should never be called before ensuring stack present")
    val currentDepth = stack.stateWrapperStackHeightsByID[wrapperID]!!
    stack.stateWrapperStackHeightsByID[wrapperID] = currentDepth - 1
}

internal class CoroutineActorStateStack : AbstractCoroutineContextElement(CoroutineActorStateStack) {
    /**
     * Key for [CoroutineActorStateStack] instance in the coroutine context.
     */
    companion object Key :
        CoroutineContext.Key<CoroutineActorStateStack>

    val stateWrapperStackHeightsByID = mutableMapOf<Int, Int>()
}