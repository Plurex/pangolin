package io.plurex.common.actors

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.plurex.common.actors.wrapper.ActorWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ActorWrapperTest {
    @Test
    fun `synchronized - allows non blocking for Unit return type`() {
        runBlocking {
            val testObj = ArbitraryActorWrapper(5)
            testObj.doSomethingUnitReturn(2)

            // Check we have returned and the value has not changed yet
            assertThat(testObj.myValue).isEqualTo(1)

            // Check we only get the value after the Unit action
            assertThat(testObj.getValue()).isEqualTo(2)
        }
    }
}


class ArbitraryActorWrapper(channelBuffer: Int = 0) : ActorWrapper(channelBuffer) {
    var myValue = 1

    suspend fun doSomethingUnitReturn(newValue: Int) = synchronizedUnit {
        delay(50)
        myValue = newValue
    }

    suspend fun getValue(): Int = synchronized {
        myValue
    }
}