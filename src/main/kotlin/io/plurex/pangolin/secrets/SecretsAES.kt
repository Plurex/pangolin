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
    key: SecretKey
) : SecretsAPI {

    private val iv = IvParameterSpec(key.encoded.sliceArray(0 until 16))
    private val encryptCypher = cipher(Cipher.ENCRYPT_MODE, key, iv)
    private val decryptCypher = cipher(Cipher.DECRYPT_MODE, key, iv)


    override fun encrypt(inData: String): String {
        val encrypted = encryptCypher.doFinal(inData.toByteArray(Charsets.UTF_8))
        return String(encoder.encode(encrypted))
    }

    override fun decrypt(encrypted: String): String {
        val byteStr = decoder.decode(encrypted.toByteArray(Charsets.UTF_8))
        return String(decryptCypher.doFinal(byteStr))
    }
}

private fun cipher(opMode: Int, secretKey: SecretKey, iv: IvParameterSpec): Cipher {
    val c = Cipher.getInstance(AES_WITH_PADDING)
    c.init(opMode, secretKey, iv)
    return c
}

private const val AES_WITH_PADDING = "AES/CBC/PKCS5Padding"
private const val AES = "AES"
private val encoder = Base64.getEncoder()
private val decoder = Base64.getDecoder()