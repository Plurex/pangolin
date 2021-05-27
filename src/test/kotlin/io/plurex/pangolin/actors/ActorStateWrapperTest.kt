package io.plurex.pangolin.actors

import io.plurex.pangolin.actors.statewrapper.ActorStateStoppedException
import io.plurex.pangolin.actors.statewrapper.ActorStateWrapper
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.IOException

class ActorStateWrapperTest {

    private val thread1Context = newSingleThreadContext("thread1")
    private val thread2Context = newSingleThreadContext("thread2")

    @Test
    fun `execute - is thread and coroutine safe`() {
        /**
         * A fairly weak test for thread safety,
         * but should fail somewhere some time if it is not thread safe.
         */
        runBlocking {
            val myState = MyArbitraryState()
            val objUnderTest = arbitraryActorWrapper(myState)
            val allJobs = mutableListOf<Job>()
            withContext(thread1Context) {
                for (i in 0..10_000) {
                    allJobs.add(launch {
                        objUnderTest.execute {
                            counter += 1
                        }
                    })
                }
            }
            withContext(thread2Context) {
                for (i in 0..10_000) {
                    allJobs.add(launch {
                        objUnderTest.execute {
                            counter -= 1
                        }
                    })
                }
            }
            allJobs.joinAll()
            Assertions.assertEquals(0, myState.counter)
            objUnderTest.stopActor()
        }
    }

    @Test
    fun `execute - handles exceptions as expected`() {
        runBlocking {
            val myState = MyArbitraryState()
            val objUnderTest = arbitraryActorWrapper(myState)
            Assertions.assertThrows(IOException::class.java) {
                runBlocking {
                    objUnderTest.execute {
                        throw IOException()
                    }
                }
            }
            // Check we can call it again.
            objUnderTest.execute {
                println("I still work")
            }
            objUnderTest.stopActor()
        }
    }

    @Test
    fun `execute - after stop`() {
        /**
         * A fairly weak test for thread safety,
         * but should fail somewhere some time if it is not thread safe.
         */
        runBlocking {
            val myState = MyArbitraryState()
            val objUnderTest = arbitraryActorWrapper(myState)
            objUnderTest.stopActor()
            Assertions.assertThrows(ActorStateStoppedException::class.java) {
                runBlocking {
                    objUnderTest.execute {
                        //do nothing
                    }
                }
            }
        }
    }

    @Test
    fun `execute - nested`() {
        runBlocking {
            val myState = MyArbitraryState()
            val objUnderTest = arbitraryActorWrapper(myState)
            objUnderTest.execute {
                counter += 1
                objUnderTest.execute {
                    counter += 1
                }
            }
            Assertions.assertEquals(2, myState.counter)
            objUnderTest.stopActor()
        }
    }


}

fun arbitraryActorWrapper(state: MyArbitraryState): ActorStateWrapper<MyArbitraryState> {
    return ActorStateWrapper(state)
}

class MyArbitraryState {
    var counter = 0
}
