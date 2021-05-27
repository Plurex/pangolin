package io.plurex.pangolin.resources

fun String.resourceContent(): String {
    val resource = ClassLoader.getSystemResource(this)
    return resource.readText()
}