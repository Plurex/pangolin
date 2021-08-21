package io.plurex.pangolin.secrets

interface SecretsAPI {

    fun encrypt(inData: String): String

    fun decrypt(encrypted: String): String

    fun encrypt(inData: ByteArray): ByteArray

    fun decrypt(encrypted: ByteArray): ByteArray

}