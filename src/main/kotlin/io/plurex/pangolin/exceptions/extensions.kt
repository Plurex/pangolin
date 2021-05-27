package io.plurex.pangolin.exceptions

fun Exception.shortDescription(): String{
    return "${this.javaClass}:${this.message}"
}

fun Throwable.shortDescription(): String{
    return "${this.javaClass}:${this.message}"
}