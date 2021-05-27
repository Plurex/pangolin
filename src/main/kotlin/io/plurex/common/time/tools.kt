package io.plurex.common.time

import kotlinx.coroutines.delay
import java.time.*

val UTC = ZoneId.of("UTC")
val DEFAULT_ZONE = UTC
var clock: Clock = Clock.systemUTC()

fun nowUTC(): ZonedDateTime {
    return Instant.ofEpochMilli(clock.instant().toEpochMilli()).atZone(ZoneId.of("UTC"))
}

fun timeNowMillis(): Long {
    return clock.instant().toEpochMilli()
}

fun org.joda.time.DateTime.toUTCZoned(): ZonedDateTime {
    return ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(millis), ZoneId.of("UTC")
    )
}

fun ZonedDateTime.toJodaDateTime(): org.joda.time.DateTime {
    return org.joda.time.DateTime(this.toInstant().toEpochMilli())
}

fun dateTimeSecondsToMillis(dateTime: String): Long {
    return LocalDateTime.parse(dateTime).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
}

fun millisToZonedDateTime(millis: Long): ZonedDateTime {
    return ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(millis), ZoneId.of("UTC")
    )
}

suspend fun delayU(millis: ULong) {
    delay(millis.toLong())
}
