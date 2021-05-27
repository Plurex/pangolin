package io.plurex.common.exceptions

fun Exception.shortDescription(): String{
    return "${this.javaClass}:${this.message}"
}

fun Throwable.shortDescription(): String{
    return "${this.javaClass}:${this.message}"
}