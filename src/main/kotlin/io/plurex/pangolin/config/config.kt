package io.plurex.pangolin.config

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

inline fun <reified CONFIG> parseConfig(path: Path): CONFIG {
    val config = ConfigFactory.parseFile(path.toFile())
    return config.extract()
}

inline fun <reified CONFIG> getConfig(configFileName: String, otherDirectoriesToSearch: List<Path> = emptyList()): CONFIG {
    val pathTryHierarchy = listOf(
        Paths.get(configFileName).toAbsolutePath()
    ) + otherDirectoriesToSearch.map {
        it.resolve(configFileName).toAbsolutePath()
    }
    pathTryHierarchy.forEach { path ->
        if (Files.exists(path) && Files.isRegularFile(path)) {
            return parseConfig(path)
        }
    }
    throw NoConfigException("Could not find $configFileName in $pathTryHierarchy")
}

class NoConfigException(msg: String = "") : Exception(msg)