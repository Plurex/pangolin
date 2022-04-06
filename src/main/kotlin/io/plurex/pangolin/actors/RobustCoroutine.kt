package io.plurex.pangolin.actors

import io.plurex.pangolin.logging.aLogger
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

private val logger = aLogger("RobustCoroutine")

fun CoroutineScope.robustCoroutine(
    name: String = "Anonymous",
    retryDelayMillis: Long = 0,
    coroutineBody: suspend () -> Unit
): RobustCoroutineBuilder {
    assert(retryDelayMillis >= 0) { "retryDelayMillis must be >= 0" }
    return RobustCoroutineBuilder(
        coroutineBody = coroutineBody,
        context = this.coroutineContext,
        retryDelayMillis = retryDelayMillis,
        name = name
    )
}

class RobustCoroutineBuilder(
    private val coroutineBody: suspend () -> Unit,
    context: CoroutineContext = Dispatchers.Unconfined,
    private val retryDelayMillis: Long,
    private val name: String
) {

    private val supervisor = SupervisorJob()
    private val scope = CoroutineScope(context + supervisor)
    private var throwableHandler: (Throwable) -> Unit = {}
    private var beforeRelaunch: () -> Unit = {}
    private var launched = false
    private val coroutineErrorHandler: CoroutineErrorHandler = { _, throwable ->
        logger.warn("($name): Error ${throwable.javaClass} ${throwable.message}", throwable)
        throwableHandler(throwable)
    }

    suspend fun launch(): Job {
        when (launched) {
            true -> throw AlreadyLaunchedException()
            false -> launched = true
        }
        val errorHandler = CoroutineExceptionHandler(coroutineErrorHandler)
        with(scope) {
            logger.debug("($name): Launching")
            launch(errorHandler) {
                try {
                    coroutineBody()
                } finally {
                    relaunch()
                }
            }

            logger.debug("($name): Launched")
        }
        return supervisor
    }

    fun beforeRelaunch(beforeRelaunchHandler: () -> Unit): RobustCoroutineBuilder {
        this.beforeRelaunch = beforeRelaunchHandler
        return this
    }

    fun handleThrowable(handler: (Throwable) -> Unit): RobustCoroutineBuilder {
        this.throwableHandler = handler
        return this
    }


    private fun relaunch() {
        val errorHandler = CoroutineExceptionHandler(coroutineErrorHandler)
        logger.debug("($name): Relaunching with delay of ${retryDelayMillis / 1000} second(s).")
        with(scope) {
            launch(errorHandler) {
                try {
                    delay(retryDelayMillis)
                    try {
                        beforeRelaunch()
                    } catch (e: java.lang.Exception) {
                        logger.warn("($name): Error during beforeRelaunch: ${e.javaClass} ${e.message}")
                        throw e
                    }
                    coroutineBody()
                } finally {
                    relaunch() //This looks like a recursion stack overflow scenario, but it seems it isn't
                }
            }
        }
    }

}


class AlreadyLaunchedException : Exception()

typealias CoroutineErrorHandler = (context: CoroutineContext, throwable: Throwable) -> Unit
