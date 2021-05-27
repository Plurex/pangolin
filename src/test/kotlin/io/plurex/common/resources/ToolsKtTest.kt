package io.plurex.common.resources

import org.junit.jupiter.api.Test

internal class ToolsKtTest {

    @Test
    fun `resourceContent - test resource`() {
        println("some_test_resource.txt".resourceContent())
    }

    @Test
    fun `resourceContent - main resource`() {
        println("some_main_resource.txt".resourceContent())
    }
}