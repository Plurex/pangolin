package io.plurex.common.actors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class ScopeParent(context: CoroutineContext? = null) : CoroutineScope {
    private val job = Job()
    override val coroutineContext = (context ?: Dispatchers.Unconfined) + job
}