package io.plurex.common.resources

fun String.resourceContent(): String {
    val resource = ClassLoader.getSystemResource(this)
    return resource.readText()
}