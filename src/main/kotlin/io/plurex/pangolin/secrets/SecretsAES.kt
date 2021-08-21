package io.plurex.pangolin.secrets

import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

enum class KeySize(val size: Int) {
    BITS_128(128),
    BITS_192(192),
    BITS_256(256)
}

fun generateSecretKey(keySize: KeySize): SecretKey {
    val generator = KeyGenerator.getInstance(AES)
    generator.init(keySize.size)
    return generator.generateKey()
}

fun SecretKey.export(): String {
    return encoder.encodeToString(this.encoded)
}

fun importSecretKey(key: String): SecretKey {
    return SecretKeySpec(decoder.decode(key), AES)
}

class SecretsAES(
    key: SecretKey,
    algo: EncryptionAlgo = EncryptionAlgo.Aes
) : SecretsAPI {

    private val encryptCipher = algo.encryptCipher(key)
    private val decryptCipher = algo.decryptCipher(key)


    override fun encrypt(inData: String): String {
        val encrypted = encryptCipher.doFinal(inData.toByteArray(Charsets.UTF_8))
        return String(encoder.encode(encrypted))
    }

    override fun encrypt(inData: ByteArray): ByteArray {
        return encryptCipher.doFinal(inData)
    }

    override fun decrypt(encrypted: String): String {
        val byteStr = decoder.decode(encrypted.toByteArray(Charsets.UTF_8))
        return String(decryptCipher.doFinal(byteStr))
    }

    override fun decrypt(encrypted: ByteArray): ByteArray {
        return decryptCipher.doFinal(encrypted)
    }
}

private const val AES_WITH_PADDING = "AES/CBC/PKCS5Padding"
private const val AES = "AES"
private val encoder = Base64.getEncoder()
private val decoder = Base64.getDecoder()


fun main() {
    println(generateSecretKey(KeySize.BITS_256).export())
}

sealed class EncryptionAlgo {
    abstract fun encryptCipher(key: SecretKey): Cipher
    abstract fun decryptCipher(key: SecretKey): Cipher

    object AesCbcPadding : EncryptionAlgo() {

        override fun encryptCipher(key: SecretKey) = cipher(Cipher.ENCRYPT_MODE, key)

        override fun decryptCipher(key: SecretKey) = cipher(Cipher.DECRYPT_MODE, key)

        private fun cipher(opMode: Int, secretKey: SecretKey): Cipher {
            val iv = IvParameterSpec(secretKey.encoded.sliceArray(0 until 16))
            val c = Cipher.getInstance(AES_WITH_PADDING)
            c.init(opMode, secretKey, iv)
            return c
        }
    }

    object Aes : EncryptionAlgo() {
        override fun encryptCipher(key: SecretKey): Cipher {
            val instance = Cipher.getInstance(AES)
            instance.init(Cipher.ENCRYPT_MODE, key)
            return instance
        }

        override fun decryptCipher(key: SecretKey): Cipher {
            val instance = Cipher.getInstance(AES)
            instance.init(Cipher.DECRYPT_MODE, key)
            return instance
        }
    }
}