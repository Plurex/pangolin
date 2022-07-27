package io.plurex.pangolin.secrets

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import io.plurex.pangolin.random.randEnum
import io.plurex.pangolin.random.randText
import org.junit.jupiter.api.Test

internal class SecretsAESTest {

    private val AES_SIZES = arrayOf(KeySize.BITS_128, KeySize.BITS_192, KeySize.BITS_256)

    @Test
    fun `encrypt - decrypt`() {
        encryptDecryptHelper(EncryptionAlgo.AesCbcPadding, *AES_SIZES)
        encryptDecryptHelper(EncryptionAlgo.Aes, *AES_SIZES)
    }

    @Test
    fun `encrypt - decrypt - bytearray`() {
        encryptDecryptByteArrayHelper(EncryptionAlgo.AesCbcPadding, *AES_SIZES)
        encryptDecryptByteArrayHelper(EncryptionAlgo.Aes, *AES_SIZES)
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
        val key = generateSecretKey(AES_SIZES.random())
        val exported = key.export()
        println(exported)
        val keyFromString = importSecretKey(exported)
        assertThat(keyFromString).isEqualTo(key)
    }
}
