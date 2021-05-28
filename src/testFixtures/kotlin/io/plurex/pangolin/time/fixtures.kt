package io.plurex.pangolin.time

import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Sets the current time to the time provided, or freezes it at current time if time is not provided.
 */
fun itIsNow(now: ZonedDateTime = LocalDateTime.now().atZone(ZoneId.of("UTC"))): ZonedDateTime {
    clock = mockk {
        every { instant() } returns now.toInstant()
    }
    return now
}

/**
 * Freezes time at the current time and returns the Unix epoch millis.
 */
fun itIsNowMillis(nowMillis: Long = timeNowMillis()): Long {
    clock = mockk {
        every { instant() } returns Instant.ofEpochMilli(nowMillis)
    }
    return nowMillis
}