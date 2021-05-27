package io.plurex.common.actors

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Test

internal class MergeChannelsTest {

    @Test
    fun `mergeChannels - channel closes when all source channels are done`() {
        runBlocking {
            val fastProducer = produce {
                for (i in 0..4) {
                    send("FAST${i}")
                }
            }

            val slowProducer = produce {
                for (i in 0..4) {
                    delay(50)
                    send("SLOW${i}")
                }
            }

            val merged = mergeChannels(listOf(fastProducer, slowProducer))

            val results = mutableListOf<String>()
            launch {
                merged.consumeEach {
                    results.add(it)
                }
            }.join()

            assertThat(results.size).isEqualTo(10)
        }
    }

    @Test
    fun `mergeChannels - consumer cancels`() {
        runBlocking {
            var fastClosed = false
            val fastProducer = produce {
                invokeOnClose { fastClosed = true }
                for (i in 0..100) {
                    delay(10)
                    send("FAST${i}")
                }
            }

            var slowClosed = false
            val slowProducer = produce {
                invokeOnClose { slowClosed = true }
                for (i in 0..100) {
                    delay(50)
                    send("SLOW${i}")
                }
            }

            val merged = mergeChannels(listOf(fastProducer, slowProducer))

            for (i in 0..5) {
                merged.receive()
            }
            merged.cancel()
            yield()
            assertThat(slowClosed).isTrue()
            assertThat(fastClosed).isTrue()
        }
    }


}