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
        encryptDecryptHelper(EncryptionAlgo.AesCbcPadding, KeySize.BITS_128, KeySize.BITS_192, KeySize.BITS_256)
        encryptDecryptHelper(EncryptionAlgo.Aes, KeySize.BITS_128, KeySize.BITS_192, KeySize.BITS_256)
    }

    @Test
    fun `encrypt - decrypt - bytearray`() {
        encryptDecryptByteArrayHelper(EncryptionAlgo.AesCbcPadding, KeySize.BITS_128, KeySize.BITS_192, KeySize.BITS_256)
        encryptDecryptByteArrayHelper(EncryptionAlgo.Aes, KeySize.BITS_128, KeySize.BITS_192, KeySize.BITS_256)
    }

    private fun encryptDecryptHelper(algo: EncryptionAlgo, vararg keySizeOptions: KeySize) {
        for (i in 0..20) {
            val key = generateSecretKey(keySizeOptions.random())
            val testObj = SecretsAES(key, algo)
            val original = randText()
            val encrypted = testObj.encrypt(original)

            assertThat(encrypted).isNotEqualTo(original)

            val decrypted = testObj.decrypt(encrypted)

            assertThat(decrypted).isEqualTo(original)
        }
    }

    private fun encryptDecryptByteArrayHelper(algo: EncryptionAlgo, vararg keySizeOptions: KeySize) {
        for (i in 0..20) {
            val key = generateSecretKey(keySizeOptions.random())
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
