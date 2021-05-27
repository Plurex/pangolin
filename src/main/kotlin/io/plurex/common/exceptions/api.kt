package io.plurex.common.exceptions

class UniqueConstraintException(val attributeName: String) : Exception() {
    fun toError() = UniqueConstraintError(attributeName)
}

data class UniqueConstraintError(val attributeName: String)

class NotFoundException(msg: String) : Exception(msg)
class ExternalDependencyException(msg: String = "") : Exception(msg)
class RelationshipIntegrityException(msg: String = "") : Exception(msg)
class NotSupportedError : Exception()
class InvalidCacheException: Exception()
