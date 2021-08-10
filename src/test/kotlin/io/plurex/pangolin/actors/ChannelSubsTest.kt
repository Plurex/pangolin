package io.plurex.pangolin.actors

import assertk.assertThat
import assertk.assertions.isTrue
import io.plurex.pangolin.random.randString
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ChannelSubsTest {

    @Test
    fun `send and receive messages with multiple subscribers`() {
        runBlocking {
            val objUnderTest = ChannelSubs<ArbitraryMessage>(name = randString(), 10)

            val messages = listOf(
                ArbitraryMessage("One"),
                ArbitraryMessage("Two")
            )

            val subscription1 = objUnderTest.newSub()
            val subscription2 = objUnderTest.newSub()

            messages.forEach {
                objUnderTest.send(it)
            }

            messages.forEach {
                Assertions.assertEquals(it, subscription1.receive())
                Assertions.assertEquals(it, subscription2.receive())
            }
        }
    }

    @Test
    fun `handle subscriptions that cancel`() {
        runBlocking {
            val objUnderTest = ChannelSubs<ArbitraryMessage>(name = randString(),10)

            val subscription1 = objUnderTest.newSub()
            subscription1.cancel()

            objUnderTest.send(ArbitraryMessage("One"))
        }
    }

    @Test
    fun `handle subscriptions that take too long`() {
        runBlocking {
            val objUnderTest = ChannelSubs<ArbitraryMessage>(name = randString(),0, sendTimeoutMillis = 100)

            val subscription1 = objUnderTest.newSub()

            objUnderTest.send(ArbitraryMessage("One"))

            assertThat(subscription1.isClosedForReceive).isTrue()
        }
    }
}

data class ArbitraryMessage(
    val id: String
)