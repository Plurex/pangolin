package io.plurex.common.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface HasLogger

inline fun <reified T : HasLogger> T.logger(): Logger =
    LoggerFactory.getLogger(T::class.java.simpleName)

fun aLogger(name: String): Logger {
    return LoggerFactory.getLogger(name)
}