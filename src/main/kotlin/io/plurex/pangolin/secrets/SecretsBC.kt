package io.plurex.pangolin.secrets

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val AES_WITH_PADDING = "AES/CBC/PKCS5Padding"
private const val AES = "AES"

private fun cipher(opMode: Int, secretKey: String, instance: String = AES): Cipher {
    if (secretKey.length != 32) throw RuntimeException("SecretKey length is not 32 chars")
    val c = Cipher.getInstance(AES)
    val sk = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")
    if (instance == AES) {
        c.init(opMode, sk)
    } else if (instance == AES_WITH_PADDING) {
        val iv = IvParameterSpec(secretKey.substring(0, 16).toByteArray(Charsets.UTF_8))
        c.init(opMode, sk, iv)
    }
    return c
}

class SecretsBC(secretKey: String) : SecretsAPI {

    private val encryptCypher = cipher(Cipher.ENCRYPT_MODE, secretKey)
    private val decryptCypher = cipher(Cipher.DECRYPT_MODE, secretKey)
    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()

    override fun encrypt(inData: String): String {
        val encrypted = encryptCypher.doFinal(inData.toByteArray(Charsets.UTF_8))
        return String(encoder.encode(encrypted))
    }

    override fun decrypt(encrypted: String): String {
        val byteStr = decoder.decode(encrypted.toByteArray(Charsets.UTF_8))
        return String(decryptCypher.doFinal(byteStr))
    }
}