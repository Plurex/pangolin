package io.plurex.pangolin.time

import io.mockk.every
import io.mockk.mockk
import java.time.*

/**
 * Sets the current time to the time provided, or freezes it at current time if time is not provided.
 *
 * Be sure to use [itIsNowRealTime] after testing to clean up.
 */
fun itIsNow(now: ZonedDateTime = LocalDateTime.now().atZone(ZoneId.of("UTC"))): ZonedDateTime {
    clock = mockk {
        every { instant() } returns now.toInstant()
    }
    return now
}

/**
 * Freezes time at the current time and returns the Unix epoch millis.
 *
 * Be sure to use [itIsNowRealTime] after testing to clean up.
 */
fun itIsNowMillis(nowMillis: Long = timeNowMillis()): Long {
    clock = mockk {
        every { instant() } returns Instant.ofEpochMilli(nowMillis)
    }
    return nowMillis
}

/**
 * Puts time back to reality
 */
fun itIsNowRealTime() {
    clock = Clock.systemUTC()
}