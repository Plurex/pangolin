package io.plurex.pangolin.actors

import io.plurex.pangolin.time.timeNowMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RobustCoroutineTest {

    @Test
    fun `robustCoroutine - can launch`() {
        runBlocking {
            var counter = 0
            val robustJob = robustCoroutine {
                counter++
            }.launch()

            yield()
            robustJob.cancelAndJoin()

            Assertions.assertEquals(1, counter)
        }
    }

    @Test
    fun `robustCoroutine - will relaunch if it stops by itself`() {
        runBlocking {
            var counter = 0
            val robustJob = robustCoroutine {
                counter++
            }.launch()

            yield()
            yield()
            robustJob.cancelAndJoin()

            Assertions.assertTrue(counter > 1)
        }
    }

    @Test
    fun `robustCoroutine - cannot launch multiple times`() {
        runBlocking {
            var counter = 0
            val builder = robustCoroutine {
                counter++
            }

            builder.launch()

            Assertions.assertThrows(AlreadyLaunchedException::class.java) {
                runBlocking { builder.launch() }
            }
        }
    }


    @Test
    fun `robustCoroutine - error in body does not kill robust coroutine`() {
        runBlocking {
            var counter = 0
            val robustJob = robustCoroutine(retryDelayMillis = 10) {
                counter++
                throw Exception("I error and never complete")
            }.launch()

            delay(30)
            robustJob.cancelAndJoin()

            Assertions.assertTrue(counter in 2..3, "Coroutine did not relaunch")
        }
    }

    @Test
    fun `robustCoroutine - can handle throwable`() {
        runBlocking {
            var errorHandled = 0
            val robustJob = robustCoroutine(retryDelayMillis = 10) {
                throw Exception("I error and never complete")
            }.handleThrowable {
                errorHandled++
            }.launch()

            delay(30)
            robustJob.cancelAndJoin()

            Assertions.assertTrue(errorHandled in 2..3, "Error handler not called")

        }
    }

    @Test
    fun `robustCoroutine - channel cancelling will just restart`() {
        runBlocking {
            var launchCount = 0
            val aChannel = Channel<Int>(10)
            launch {
                for (i in 1..2) {
                    aChannel.send(i)
                }
            }
            val robustJob = withContext(newSingleThreadContext("rj")) {
                robustCoroutine(retryDelayMillis = 0) {
                    launchCount++
                    for (i in aChannel) {
                    }
                }.launch()
            }
            aChannel.cancel()

            val start = timeNowMillis()
            while(launchCount <= 1 && timeNowMillis() - start < 2000){
                delay(50)
            }
            robustJob.cancelAndJoin()
            Assertions.assertTrue(launchCount > 1, "Was not relaunched")
        }
    }

    @Test
    fun `robustCoroutine - can provide beforeRelaunch logic`() {
        runBlocking {
            var beforeRelaunch = 0
            val robustJob = robustCoroutine(retryDelayMillis = 10) {
                throw Exception("I error and never complete")
            }.beforeRelaunch {
                beforeRelaunch++
            }.launch()

            delay(30)
            robustJob.cancelAndJoin()

            Assertions.assertTrue(beforeRelaunch in 2..3, "relaunch not called")
        }
    }


    @Test
    fun `robustCoroutine - can handle error in beforeRelaunch logic`() {
        runBlocking {
            var beforeRelaunch = 0
            val robustJob = robustCoroutine(retryDelayMillis = 10) {
                throw Exception("I error and never complete")
            }.beforeRelaunch {
                beforeRelaunch++
                throw Exception("I am an error in before relaunch")
            }.launch()

            val start = timeNowMillis()
            while (beforeRelaunch <= 2 && timeNowMillis() - start < 2000) {
                delay(50)
            }
            robustJob.cancelAndJoin()
            Assertions.assertTrue(beforeRelaunch > 2, "relaunch not called")
        }
    }


    @Test
    fun `robustCoroutine - parent joins when robust coroutine is finished`() {
        runBlocking {
            var counter = 0
            val parent = launch {
                robustCoroutine {
                    delay(10)
                    counter++
                }.launch()
            }
            parent.join()
        }
    }


}