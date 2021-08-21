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
        encryptDecryptHelper(EncryptionAlgo.AesCbcPadding)
        encryptDecryptHelper(EncryptionAlgo.Aes)
    }

    @Test
    fun `encrypt - decrypt - bytearray`() {
        encryptDecryptByteArrayHelper(EncryptionAlgo.AesCbcPadding)
        encryptDecryptByteArrayHelper(EncryptionAlgo.Aes)
    }

    private fun encryptDecryptHelper(algo: EncryptionAlgo) {
        for (i in 0..20) {
            val key = generateSecretKey(randEnum())
            val testObj = SecretsAES(key, algo)
            val original = randText()
            val encrypted = testObj.encrypt(original)

            assertThat(encrypted).isNotEqualTo(original)

            val decrypted = testObj.decrypt(encrypted)

            assertThat(decrypted).isEqualTo(original)
        }
    }

    private fun encryptDecryptByteArrayHelper(algo: EncryptionAlgo) {
        for (i in 0..20) {
            val key = generateSecretKey(randEnum())
            val testObj = SecretsAES(key, algo)
            val original = randText().toByteArray()
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