package io.plurex.pangolin.secrets

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import io.plurex.pangolin.random.randString
import org.junit.jupiter.api.Test

internal class SecretsBCTest {

    @Test
    fun `encrypt - decrypt`() {
        val toEncrypt = randString()
        val secretKey = "4h4fUw+zhnW+G9WNTpy7EF6A6YHkWGjv"

        val testObj = SecretsBC(secretKey)

        val encrypted = testObj.encrypt(toEncrypt)

        assertThat(encrypted).isNotEqualTo(toEncrypt)

        val decrypted = testObj.decrypt(encrypted)

        assertThat(decrypted).isEqualTo(toEncrypt)
    }
}