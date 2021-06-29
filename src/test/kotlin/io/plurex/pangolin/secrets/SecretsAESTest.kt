package io.plurex.pangolin.secrets

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import io.plurex.pangolin.random.randEnum
import io.plurex.pangolin.random.randText
import org.junit.jupiter.api.Test

internal class SecretsAESTest {

    @Test
    fun `encrypt - decrypt`() {
        for (i in 0..20) {
            val key = generateSecretKey(randEnum())
            val testObj = SecretsAES(key)
            val original = randText()
            val encrypted = testObj.encrypt(original)

            assertThat(encrypted).isNotEqualTo(original)

            val decrypted = testObj.decrypt(encrypted)

            assertThat(decrypted).isEqualTo(original)
        }
    }

    @Test
    fun importSecretKey() {
        val key = generateSecretKey(randEnum())
        val exported = key.export()
        println(exported)
        val keyFromString = importSecretKey(exported)
        assertThat(keyFromString).isEqualTo(key)
    }
}