package io.plurex.common.collections

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

internal class MaxSizeMapTest {

    @Test
    fun `put when full stays the same size`() {
        val testObj = MaxSizeMap<String, String>(1)

        testObj["k1"] = "v1"
        testObj["k2"] = "v2"

        assertThat(testObj["k2"]).isEqualTo("v2")
        assertThat(testObj.size).isEqualTo(1)
    }

}
